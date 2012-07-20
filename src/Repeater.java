package nz.gen.geek_central.gles2_try;
/*
    Execution of a repeating task with timing.

    Copyright 2012 by Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy of
    the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    License for the specific language governing permissions and limitations under
    the License.
*/

public class Repeater implements Runnable
  {
    public static final float MaxFactor = 0.8f; /* proportion of CPU time to consume */
    private final Runnable PreRun, Run;
    private final android.os.Handler DoRun;
    private boolean Running = false;
    public long ThisRun, LastRun, LastTimeTaken;

    public Repeater
      (
        Runnable PreRun,
          /* optional--if specified, then it takes the responsibility
            to invoke DoTask (below) to run Run */
        Runnable Run
      )
      {
        this.PreRun = PreRun;
        this.Run = Run;
        this.DoRun = new android.os.Handler();
        LastRun = 0;
        LastTimeTaken = 0;
      } /*Repeater*/

    public Repeater
      (
        Runnable Run
      )
      {
        this(null, Run);
      } /*Repeater*/

    private void Requeue()
      {
        if (Running)
          {
            DoRun.postAtTime(this, LastRun + (long)(LastTimeTaken / MaxFactor));
          } /*if*/
      } /*Requeue*/

    public void Start()
      /* starts repeating the task execution. */
      {
        if (!Running)
          {
            Running = true;
            Requeue();
          } /*if*/
      } /*Start*/

    public void Stop()
      /* stops repeating the task execution. */
      {
        Running = false;
        DoRun.removeCallbacks(this);
      } /*Stop*/

    public void run()
      {
        if (Running)
          {
            if (PreRun != null)
              {
                PreRun.run();
              }
            else
              {
                DoTask();
              } /*if*/
          } /*if*/
      } /*run*/

    public void DoTask()
      /* performs the Run task and records timing. */
      {
        ThisRun = android.os.SystemClock.uptimeMillis();
        Run.run();
        LastTimeTaken = android.os.SystemClock.uptimeMillis() - ThisRun;
        LastRun = ThisRun;
        Requeue();
      } /*DoTask*/

    public String Stats()
      {
        return
            String.format
              (
                "%dms@%.2ffps",
                LastTimeTaken,
                1000.0 / (ThisRun - LastRun)
              );
      } /*Stats*/

  } /*Repeater*/
