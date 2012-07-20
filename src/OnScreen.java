package nz.gen.geek_central.gles2_try;
/*
    Direct onscreen display of sample animation.

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

public class OnScreen extends android.app.Activity
  {
    OnScreenView TheOnScreenView;
    android.widget.TextView StatsView;

    @Override
    public void onCreate
      (
        android.os.Bundle SavedInstanceState
      )
      {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.onscreen);
        TheOnScreenView = (OnScreenView)findViewById(R.id.main);
        StatsView = (android.widget.TextView)findViewById(R.id.stats);
        TheOnScreenView.ShowStats =
            new OnScreenView.StatsCallback()
              {
                public void ShowStats
                  (
                    final String Stats
                  )
                  {
                    runOnUiThread
                      (
                        new Runnable()
                          {
                            public void run()
                              {
                                StatsView.setText(Stats);
                              } /*run*/
                          } /*Runnable*/
                      );
                  } /*ShowStats*/
              } /*StatsCallback*/;
      } /*onCreate*/

    @Override
    public void onPause()
      {
        super.onPause();
        TheOnScreenView.onPause();
      } /*onPause*/

    @Override
    public void onResume()
      {
        super.onResume();
        TheOnScreenView.onResume();
      } /*onResume*/

  } /*OnScreen*/
