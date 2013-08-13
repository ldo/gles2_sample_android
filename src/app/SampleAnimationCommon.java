package nz.gen.geek_central.gles2_sample;
/*
    Common management of all animation displays, including timing.

    Copyright 2013 by Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.

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

import nz.gen.geek_central.GLUseful.Vec3f;
import nz.gen.geek_central.GLUseful.Mat4f;
import nz.gen.geek_central.GLUseful.GLUseful;
import static nz.gen.geek_central.GLUseful.GLUseful.gl;

public abstract class SampleAnimationCommon
  {
    protected final android.content.Context ctx;
    protected double StartTime, LastDraw;
    protected Mat4f ProjectionMatrix;
    public long ThisRun, LastRun, LastTimeTaken; /* milliseconds */
    public int ViewWidth, ViewHeight, ViewSize;
    public double SmoothedTimeTaken; /* in seconds */
    public static final int SmoothFactor = 20;
      /* for computing rolling-average frame rate, must be > 1 */

    public final int NullColor;

    public SampleAnimationCommon
      (
        android.content.Context ctx
      )
      /* must be called by subclass constructors; note no GL
        calls should be made in constructors; defer these to Bind,
        Unbind, Setup and OnDraw. */
      {
        this.ctx = ctx;
        NullColor = ctx.getResources().getColor(R.color.nothing);
        StartTime = System.currentTimeMillis() / 1000.0;
        SmoothedTimeTaken = 1.0; /* just to avoid infinite frame rate */
      } /*SampleAnimationCommon*/

    public void Bind()
      /* override to do allocation of GL resources. */
      {
      } /*Bind*/

    public void Unbind
      (
        boolean Release
          /* true iff GL context still valid, so explicitly free up allocated resources.
            false means GL context has gone (or is going), so simply forget allocated
            GL resources without making any GL calls. */
      )
      /* override to free up GL resources associated with this object. */
      {
      } /*Unbind*/

    public void Setup
      (
        int ViewWidth,
        int ViewHeight
      )
      /* initial setup for drawing that doesn't need to be done for every frame. */
      {
        Bind();
        gl.glEnable(gl.GL_CULL_FACE);
        gl.glEnable(gl.GL_DEPTH_TEST);
        gl.glViewport(0, 0, ViewWidth, ViewHeight);
        this.ViewWidth = ViewWidth;
        this.ViewHeight = ViewHeight;
        this.ViewSize = Math.min(ViewWidth, ViewHeight);
        ProjectionMatrix =
                Mat4f.frustum
                  (
                    /*L =*/ - (float)ViewWidth / ViewHeight,
                    /*R =*/ (float)ViewWidth / ViewHeight,
                    /*B =*/ -1.0f,
                    /*T =*/ 1.0f,
                    /*N =*/ 1.0f,
                    /*F =*/ 10.0f
                  )
            .mul(
                Mat4f.translation(new Vec3f(0, 0, -3.0f))
            );
      } /*Setup*/

    abstract protected void OnDraw
      (
        double AtTime
      );
      /* Override this to do your drawing. */

    public void Draw
      (
        double AtTime
      )
      /* clears display, calls OnDraw and updates statistics. */
      {
        ThisRun = android.os.SystemClock.uptimeMillis();
        GLUseful.ClearColor(new GLUseful.Color(NullColor));
        gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
        OnDraw(AtTime);
        LastTimeTaken = android.os.SystemClock.uptimeMillis() - ThisRun;
        SmoothedTimeTaken =
                (SmoothedTimeTaken * (SmoothFactor - 1) + LastTimeTaken / 1000.0)
            /
                SmoothFactor;
        LastRun = ThisRun;
      } /*Draw*/

    public void Draw()
      /* draws the current animation frame. */
      {
        LastDraw = System.currentTimeMillis() / 1000.0 - StartTime;
        Draw(LastDraw);
      } /*Draw*/

    public void DrawAgain()
      /* redraws the same animation frame as last time. */
      {
        Draw(LastDraw);
      } /*DrawAgain*/

    public double GetDrawTime()
      {
        return
            LastDraw;
      } /*GetDrawTime*/

    public void SetDrawTime
      (
        double AtTime /* ignored if negative */
      )
      {
        if (AtTime >= 0.0)
          {
            StartTime = System.currentTimeMillis() / 1000.0 - AtTime;
            LastDraw = AtTime;
          } /*if*/
      } /*SetDrawTime*/

  } /*SampleAnimationCommon*/;
