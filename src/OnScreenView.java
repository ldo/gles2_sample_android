package nz.gen.geek_central.gles2_sample;
/*
    Onscreen display of sample animation--the GLSurfaceView where the
    animation takes place.

    Copyright 2012, 2013 by Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.

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

public class OnScreenView extends android.opengl.GLSurfaceView
  {
    public enum Animations /* all the sample animation classes listed here */
      {
        SmoothShadedArrow(R.string.smooth_shaded_arrow, SpinningArrow.SmoothShaded.class),
        FlatShadedArrow(R.string.flat_shaded_arrow, SpinningArrow.FlatShaded.class),
        WireframeArrow(R.string.wireframe_arrow, SpinningArrow.Wireframe.class),
        ;

        public final int NameID;
        public final Class<? extends SampleAnimationCommon> AnimClass;

        private Animations
          (
            int NameID,
            Class<? extends SampleAnimationCommon> AnimClass
          )
          {
            this.NameID = NameID;
            this.AnimClass = AnimClass;
          } /*Animations*/

        public static Animations WithName
          (
            int NameID
          )
          /* returns the Animations value with the specified name. */
          {
            Animations Result;
            for (int i = 0;;)
              {
                if (values()[i].NameID == NameID)
                  {
                    Result = values()[i];
                    break;
                  } /*if*/
                ++i;
              } /*for*/
            return
                Result;
          } /*WithName*/

      } /*Animations*/;

    android.content.Context TheContext;
    public android.widget.TextView StatsView;
    int ViewWidth, ViewHeight;
    double SavedDrawTime = -1.0;
    Animations CurAnimationChoice;
    SampleAnimationCommon CurAnimation;
    boolean NeedSetup;

    private void StartAnimation()
      /* actually instantiates the currently-chosen animation class.
        Doesn't do any GL calls. */
      {
        try
          {
            CurAnimation =
                CurAnimationChoice.AnimClass.getConstructor(android.content.Context.class)
                .newInstance(TheContext);
          }
        catch (NoSuchMethodException Fail)
          {
            throw new RuntimeException(Fail.toString());
          }
        catch (InstantiationException Fail)
          {
            throw new RuntimeException(Fail.toString());
          }
        catch (IllegalAccessException Fail)
          {
            throw new RuntimeException(Fail.toString());
          }
        catch (IllegalArgumentException Fail)
          {
            throw new RuntimeException(Fail.toString());
          }
        catch (java.lang.reflect.InvocationTargetException Fail)
          {
            throw new RuntimeException(Fail.toString());
          } /*try*/
        CurAnimation.SetDrawTime(SavedDrawTime);
        SavedDrawTime = -1.0;
        NeedSetup = true;
      } /*StartAnimation*/

    private class OnScreenViewRenderer implements Renderer
      {
      /* Note I ignore the passed GL10 argument, and exclusively use
        static methods from GLES20 class for all OpenGL drawing */

        public void onDrawFrame
          (
            GL10 _gl
          )
          {
            if (CurAnimation != null)
              {
                if (NeedSetup)
                  {
                    CurAnimation.Setup(ViewWidth, ViewHeight);
                    NeedSetup = false;
                  } /*if*/
                CurAnimation.Draw();
                if (StatsView != null && StatsView.getHandler() != null)
                  {
                    final String Stats = String.format
                      (
                        "%dms@%.2f(%.2f)fps",
                        CurAnimation.LastTimeTaken,
                        1.0 / CurAnimation.SmoothedTimeTaken,
                        1000.0 / (CurAnimation.ThisRun - CurAnimation.LastRun)
                      );
                    StatsView.post
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
              } /*if*/
          } /*onDrawFrame*/

        public void onSurfaceChanged
          (
            GL10 _gl,
            int ViewWidth,
            int ViewHeight
          )
          {
            if (CurAnimationChoice != null)
              {
                if (CurAnimation == null)
                  {
                    StartAnimation();
                  } /*if*/
                OnScreenView.this.ViewWidth = ViewWidth;
                OnScreenView.this.ViewHeight = ViewHeight;
                CurAnimation.Setup(ViewWidth, ViewHeight);
                NeedSetup = false;
              } /*if*/
          } /*onSurfaceChanged*/

        public void onSurfaceCreated
          (
            GL10 _gl,
            javax.microedition.khronos.egl.EGLConfig Config
          )
          {
          /* leave all work to onSurfaceChanged */
          } /*onSurfaceCreated*/

      } /*OnScreenViewRenderer*/;

    final OnScreenViewRenderer Render = new OnScreenViewRenderer();

    public OnScreenView
      (
        android.content.Context TheContext,
        android.util.AttributeSet TheAttributes
      )
      {
        super(TheContext, TheAttributes);
        this.TheContext = TheContext;
        setEGLContextClientVersion(2);
        setRenderer(Render);
      /* setRenderMode(RENDERMODE_CONTINUOUSLY); */ /* default */
        SetAnimation(Animations.values()[0]);
      } /*OnScreenView*/

    public void SetAnimation
      (
        Animations NewAnimation
      )
      {
        if (NewAnimation != CurAnimationChoice)
          {
            final Animations NewAnimationChoice = NewAnimation;
            queueEvent /* synchronize with animation drawing */
              (
                new Runnable()
                  {
                    public void run()
                      {
                        if (CurAnimation != null)
                          {
                            SavedDrawTime = CurAnimation.GetDrawTime(); /* preserve animation continuity */
                            CurAnimation.Unbind(true);
                            CurAnimation = null;
                          } /*if*/
                        CurAnimationChoice = NewAnimationChoice;
                        StartAnimation();
                      } /*run*/
                  } /*Runnable*/
              );
          } /*if*/
      } /*SetAnimation*/

    public Animations GetAnimation()
      {
        return
            CurAnimationChoice;
      } /*GetAnimation*/

    @Override
    public void onPause()
      {
        if (CurAnimation != null)
          {
            SavedDrawTime = CurAnimation.GetDrawTime(); /* preserve animation continuity */
            CurAnimation.Unbind(false); /* losing the GL context */
            CurAnimation = null;
          } /*if*/
        super.onPause();
      } /*onPause*/

/*
    Implementation of saving/restoring instance state. Doing this
    allows me to transparently restore state of animation if system
    needs to kill me while I'm in the background, or on an orientation
    change while I'm in the foreground.
*/

    @Override
    public android.os.Parcelable onSaveInstanceState()
      {
        final android.os.Bundle MyState = new android.os.Bundle();
        if (CurAnimationChoice != null)
          {
            MyState.putInt("AnimationName", CurAnimationChoice.NameID);
            if (CurAnimation != null)
              {
                MyState.putDouble("DrawTime", CurAnimation.GetDrawTime());
              } /*if*/
          } /*if*/
        return
            new BundledSavedState(super.onSaveInstanceState(), MyState);
      } /*onSaveInstanceState*/

    @Override
    public void onRestoreInstanceState
      (
        android.os.Parcelable ToRestore
      )
      {
        super.onRestoreInstanceState(((BundledSavedState)ToRestore).SuperState);
        final android.os.Bundle MyState = ((BundledSavedState)ToRestore).MyState;
        final int AnimationName = MyState.getInt("AnimationName", 0);
        if (AnimationName != 0)
          {
            SetAnimation(Animations.WithName(AnimationName));
          } /*if*/
        SavedDrawTime = MyState.getDouble("DrawTime", -1.0);
      } /*onRestoreInstanceState*/

  } /*OnScreenView*/;
