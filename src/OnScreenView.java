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
import nz.gen.geek_central.android.useful.BundledSavedState;
import nz.gen.geek_central.GraphicsUseful.PaintBuilder;
import nz.gen.geek_central.GLUseful.Vec3f;
import nz.gen.geek_central.GLUseful.Mat4f;
import nz.gen.geek_central.GLUseful.GLUseful;
import nz.gen.geek_central.GLUseful.GLView;
import static nz.gen.geek_central.GLUseful.GLUseful.gl;

public class OnScreenView extends android.opengl.GLSurfaceView
  {
    public android.widget.TextView StatsView;
    final static boolean DefaultShaded = true;
    boolean Shaded;
    double SetDrawTime = -1.0;
    int LastViewWidth = 0, LastViewHeight = 0;
    long ThisRun, LastRun, LastTimeTaken;

    private final int NullColor = getResources().getColor(R.color.nothing);

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
            if (ArrowShape == null)
              {
                ArrowShape = new SpinningArrow(getContext(), Shaded);
                ArrowShape.Setup(LastViewWidth, LastViewHeight);
              /* restoring instance state */
                Render.ArrowShape.SetDrawTime(SetDrawTime);
                SetDrawTime = - 1.0;
              } /*if*/
            ThisRun = android.os.SystemClock.uptimeMillis();
            GLUseful.ClearColor(new GLUseful.Color(NullColor));
            gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
            if (Background != null)
              {
                Background.Draw
                  (
                    /*Projection =*/ BGProjection,
                    /*Left =*/ -1.0f,
                    /*Bottom =*/ -1.0f,
                    /*Right =*/ 1.0f,
                    /*Top =*/ 1.0f,
                    /*Depth =*/ 0.99f /*disappears on some devices at 1.0f*/
                  );
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
                /*BitsHeight =*/ ViewSize
              );
              {
                final float ViewRadius = ViewSize / 2.0f;
                final android.graphics.Canvas g = Background.Draw;
                g.save();
                g.translate(ViewRadius, ViewRadius);
                g.drawColor(NullColor, android.graphics.PorterDuff.Mode.SRC);
                  /* initialize all pixels to fully transparent */
                g.drawArc
                  (
                    /*oval =*/ new android.graphics.RectF(-ViewRadius, -ViewRadius, ViewRadius, ViewRadius),
                    /*startAngle =*/ 0.0f,
                    /*sweepAngle =*/ 360.0f,
                    /*useCenter =*/ false,
                    /*paint =*/ new PaintBuilder(true)
                        .setStyle(android.graphics.Paint.Style.FILL)
                        .setColor(getResources().getColor(R.color.background))
                        .get()
                  );
                  {
                    final String TheText = "Background Text";
                    final android.graphics.Paint TextPaint = new PaintBuilder(true)
                        .setTextSize(getResources().getDimension(R.dimen.background_text_size))
                        .setTextAlign(android.graphics.Paint.Align.CENTER)
                        .setColor(getResources().getColor(R.color.background_text))
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
            ArrowShape = new SpinningArrow(getContext(), Shaded);
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
        Shaded = DefaultShaded;
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
        if (NewShaded != Shaded)
          {
            queueEvent
              (
                new Runnable()
                  {
                    public void run()
                      {
                        if (Render.ArrowShape != null)
                          {
                            SetDrawTime = Render.ArrowShape.GetDrawTime(); /* preserve animation continuity */
                            Render.ArrowShape.Release();
                            Render.ArrowShape = null;
                            Shaded = NewShaded;
                          } /*if*/
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

/*
    Implementation of saving/restoring instance state. Doing this
    allows me to transparently restore state of animation and
    rendering if system needs to kill me while I'm in the background,
    or on an orientation change while I'm in the foreground.
*/

    @Override
    public android.os.Parcelable onSaveInstanceState()
      {
        final android.os.Bundle MyState = new android.os.Bundle();
        MyState.putBoolean("DrawShaded", Shaded);
        MyState.putDouble
          (
            "ArrowDrawTime",
            Render.ArrowShape != null ? Render.ArrowShape.GetDrawTime() : -1.0
          );
        return
            new BundledSavedState(super.onSaveInstanceState(), MyState);
      } /*onSaveInstanceState*/

    @Override
    public void onRestoreInstanceState
      (
        android.os.Parcelable SavedState
      )
      {
        super.onRestoreInstanceState(((BundledSavedState)SavedState).SuperState);
        final android.os.Bundle MyState = ((BundledSavedState)SavedState).MyState;
        Shaded = MyState.getBoolean("DrawShaded", DefaultShaded);
        SetDrawTime = MyState.getDouble("ArrowDrawTime", -1.0);
      } /*onRestoreInstanceState*/

  } /*OnScreenView*/
