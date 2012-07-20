package nz.gen.geek_central.gles2_sample;
/*
    Direct onscreen display of sample animation--the GLSurfaceView where the
    animation takes place.

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

import javax.microedition.khronos.opengles.GL10;

public class OnScreenView
    extends android.opengl.GLSurfaceView
    implements Runnable
  {
    public interface StatsCallback
      {
        public void ShowStats
          (
            String Stats
          );
      } /*StatsCallback*/

    StatsCallback ShowStats;

    private class OnScreenViewRenderer
        implements
            Renderer,
            Runnable
      {
      /* Note I ignore the passed GL10 argument, and exclusively use
        static methods from GLES20 class for all OpenGL drawing */
        final SpinningArrow ArrowShape = new SpinningArrow();
        final Repeater Repeating;

        public OnScreenViewRenderer()
          {
            super();
            Repeating = new Repeater(OnScreenView.this, this);
          } /*OnScreenViewRenderer*/

        public void Start()
          {
            Repeating.Start();
          } /*Start*/

        public void Stop()
          {
            Repeating.Stop();
          } /*Stop*/

        public void onDrawFrame
          (
            GL10 _gl
          )
          {
            Repeating.DoTask();
            if (ShowStats != null)
              {
                ShowStats.ShowStats(Repeating.Stats());
              } /*if*/
          } /*onDrawFrame*/

        public void run()
          {
            ArrowShape.Draw();
          } /*run*/

        public void onSurfaceChanged
          (
            GL10 _gl,
            int ViewWidth,
            int ViewHeight
          )
          {
            ArrowShape.Setup(ViewWidth, ViewHeight);
          } /*onSurfaceChanged*/

        public void onSurfaceCreated
          (
            GL10 _gl,
            javax.microedition.khronos.egl.EGLConfig Config
          )
          {
          /* do everything in onSurfaceChanged */
          } /*onSurfaceCreated*/

      } /*OnScreenViewRenderer*/

    final OnScreenViewRenderer Render = new OnScreenViewRenderer();

    public OnScreenView
      (
        android.content.Context TheContext,
        android.util.AttributeSet TheAttributes
      )
      {
        super(TheContext, TheAttributes);
        setEGLContextClientVersion(2);
        setRenderer(Render);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
      } /*OnScreenView*/

    public void run()
      {
        requestRender();
      } /*run*/

    @Override
    public void onPause()
      {
        Render.Stop();
        super.onPause();
      } /*onPause*/

    @Override
    public void onResume()
      {
        super.onResume();
        Render.Start();
      } /*onResume*/

  } /*OnScreenView*/
