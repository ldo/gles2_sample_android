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
import nz.gen.geek_central.GraphicsUseful.PaintBuilder;
import nz.gen.geek_central.GLUseful.Vec3f;
import nz.gen.geek_central.GLUseful.Mat4f;
import nz.gen.geek_central.GLUseful.GLView;

public class OnScreenView extends android.opengl.GLSurfaceView
  {
    static final android.opengl.GLES20 gl = nz.gen.geek_central.GLUseful.GLUseful.gl; /* for easier references */

    public android.widget.TextView StatsView;
    boolean Shaded, NewShaded; /* fixme: need to preserve last-set value across orientation changes */
    int LastViewWidth = 0, LastViewHeight = 0;
    long ThisRun, LastRun, LastTimeTaken;

    private class OnScreenViewRenderer implements Renderer
      {
      /* Note I ignore the passed GL10 argument, and exclusively use
        static methods from GLES20 class for all OpenGL drawing */
        SpinningArrow ArrowShape;
        GLView Background;
        Mat4f BGProjection;

        public void onDrawFrame
          (
            GL10 _gl
          )
          {
            if (NewShaded != Shaded)
              {
                if (ArrowShape != null)
                  {
                    ArrowShape.Release();
                  } /*if*/
                ArrowShape = null; /* allocate a new one */
                Shaded = NewShaded;
              } /*if*/
            if (ArrowShape == null)
              {
                ArrowShape = new SpinningArrow(Shaded);
                ArrowShape.Setup(LastViewWidth, LastViewHeight);
              } /*if*/
            ThisRun = android.os.SystemClock.uptimeMillis();
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
            if (Background != null)
              {
                Background.Draw(BGProjection);
              } /*if*/
            ArrowShape.Draw();
            LastTimeTaken = android.os.SystemClock.uptimeMillis() - ThisRun;
            if (StatsView != null)
              {
                final String Stats = String.format
                  (
                    "%dms@%.2ffps",
                    LastTimeTaken,
                    1000.0 / (ThisRun - LastRun)
                  );
                getHandler().post
                  (
                    new Runnable()
                      {
                        public void run()
                          {
                            StatsView.setText(Stats);
                          } /*run*/
                      } /*Runnable*/
                  );
              } /*if*/
            LastRun = ThisRun;
          } /*onDrawFrame*/

        public void onSurfaceChanged
          (
            GL10 _gl,
            int ViewWidth,
            int ViewHeight
          )
          {
            gl.glEnable(gl.GL_CULL_FACE);
            gl.glEnable(gl.GL_DEPTH_TEST);
            gl.glViewport(0, 0, ViewWidth, ViewHeight);
            LastViewWidth = ViewWidth;
            LastViewHeight = ViewHeight;
            if (ArrowShape != null)
              {
                ArrowShape.Setup(ViewWidth, ViewHeight);
              } /*if*/
            if (Background != null)
              {
                Background.Release();
              } /*if*/
            final int ViewSize = Math.min(ViewWidth, ViewHeight);
            Background = new GLView
              (
                /*BitsWidth =*/ ViewSize,
                /*BitsHeight =*/ ViewSize,
                /*Left =*/ -1.0f,
                /*Bottom =*/ -1.0f,
                /*Right =*/ 1.0f,
                /*Top =*/ 1.0f,
                /*Depth =*/ 0.99f /*disappears on some devices at 1.0f*/
              );
              {
                final float ViewRadius = ViewSize / 2.0f;
                final android.graphics.Canvas g = Background.Draw;
                g.save();
                g.translate(ViewRadius, ViewRadius);
                g.drawColor(0, android.graphics.PorterDuff.Mode.SRC);
                  /* initialize all pixels to fully transparent */
                g.drawArc
                  (
                    /*oval =*/ new android.graphics.RectF(-ViewRadius, -ViewRadius, ViewRadius, ViewRadius),
                    /*startAngle =*/ 0.0f,
                    /*sweepAngle =*/ 360.0f,
                    /*useCenter =*/ false,
                    /*paint =*/ new PaintBuilder()
                        .setStyle(android.graphics.Paint.Style.FILL)
                        .setColor(0xff0a6d01)
                        .get()
                  );
                  {
                    final String TheText = "Background Text";
                    final android.graphics.Paint TextPaint = new PaintBuilder()
                        .setTextSize(36.0f)
                        .setTextAlign(android.graphics.Paint.Align.CENTER)
                        .setColor(0xfffff4aa)
                        .get();
                    final android.graphics.Rect TextBounds = new android.graphics.Rect();
                    TextPaint.getTextBounds(TheText, 0, TheText.length(), TextBounds);
                    final float YOffset = - (TextBounds.bottom + TextBounds.top) / 2.0f;
                      /* for vertical centring */
                    g.drawText(TheText, - ViewRadius / 2.0f, - ViewRadius / 2.0f + YOffset, TextPaint);
                    g.drawText(TheText, ViewRadius / 2.0f, ViewRadius / 2.0f + YOffset, TextPaint);
                  }
                g.restore();
                Background.DrawChanged();
              }
            BGProjection = Mat4f.scaling
              (
                /*sx =*/ ViewWidth > ViewHeight ? ViewHeight * 1.0f / ViewWidth : 1.0f,
                /*sy =*/ ViewHeight > ViewWidth ? ViewWidth * 1.0f / ViewHeight : 1.0f,
                /*sz =*/ 1.0f
              );
          } /*onSurfaceChanged*/

        public void onSurfaceCreated
          (
            GL10 _gl,
            javax.microedition.khronos.egl.EGLConfig Config
          )
          {
            ArrowShape = new SpinningArrow(Shaded);
              /* leave actual setup to onSurfaceChanged */
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
        Shaded = true; /* default */
        NewShaded = Shaded;
        setEGLContextClientVersion(2);
        setRenderer(Render);
      /* setRenderMode(RENDERMODE_CONTINUOUSLY); */ /* default */
      } /*OnScreenView*/

    public boolean GetShaded()
      {
        return
            Shaded;
      } /*GetShaded*/

    public void SetShaded
      (
        final boolean NewShaded
      )
      {
        if (this.NewShaded != NewShaded)
          {
            queueEvent
              (
                new Runnable()
                  {
                    public void run()
                      {
                      /* note I don't dispose of ArrowShape here, even though I'm
                        on the GL thread, just in case the GL context is not actually
                        set properly */
                        OnScreenView.this.NewShaded = NewShaded;
                      } /*run*/
                  } /*Runnable*/
              );
          } /*if*/
      } /*Reset*/

    @Override
    public void onPause()
      {
        super.onPause();
        Render.ArrowShape = null; /* losing the GL context anyway */
        Render.Background = null;
      } /*onPause*/

  } /*OnScreenView*/
