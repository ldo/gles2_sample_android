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

  /* things that should only be accessed on UI thread: */
    android.content.Context TheContext;
    public android.widget.TextView StatsView;

    private class OnScreenViewRenderer implements Renderer
      {
      /* Note I ignore the passed GL10 argument, and exclusively use
        static methods from GLES20 class for all OpenGL drawing */

      /* state that should only be changed on renderer thread: */
        private int ViewWidth, ViewHeight;
        private boolean NeedSetup;
        public double SavedDrawTime = -1.0;
        public Animations CurAnimationChoice;
        public SampleAnimationCommon CurAnimation;

        public void StartAnimation()
          /* actually instantiates the currently-chosen animation class.
            Doesn't do any GL calls, but must run on renderer thread. */
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

        public void StopAnimation()
          {
            if (CurAnimation != null)
              {
                SavedDrawTime = CurAnimation.GetDrawTime(); /* preserve animation continuity */
                CurAnimation.Unbind(true);
                CurAnimation = null;
              } /*if*/
          } /*StopAnimation*/

        public void Synchronize
          (
            final Runnable Task
          )
          /* runs Task on the renderer thread and waits for it to complete. */
          {
            final Object Sync = new Object();
            synchronized (Sync)
              {
                queueEvent
                  (
                    new Runnable()
                      {
                        public void run()
                          {
                            Task.run();
                            synchronized (Sync)
                              {
                                Sync.notify();
                              } /*synchronized*/
                          } /*run*/
                      } /*Runnable*/
                  );
                for (;;)
                  {
                    try
                      {
                        Sync.wait();
                        break;
                      }
                    catch (InterruptedException HoHum)
                      {
                      /* keep waiting */
                      } /*try*/
                  } /*for*/
              } /*synchronized*/
          } /*Synchronize*/

        public void onDrawFrame
          (
            GL10 _gl
          )
          {
            if (CurAnimation != null)
              {
                final SampleAnimationCommon CurAnimation = this.CurAnimation;
                  /* to avoid race conditions in access from UI-thread task below */
                if (NeedSetup)
                  {
                    CurAnimation.Setup(ViewWidth, ViewHeight);
                    NeedSetup = false;
                  } /*if*/
                CurAnimation.Draw();
                post
                  (
                    new Runnable()
                      {
                        public void run()
                          {
                            if (StatsView != null)
                              {
                                final String Stats = String.format
                                  (
                                    "%dms@%.2f(%.2f)fps",
                                    CurAnimation.LastTimeTaken,
                                    1.0 / CurAnimation.SmoothedTimeTaken,
                                    1000.0 / (CurAnimation.ThisRun - CurAnimation.LastRun)
                                  );
                                StatsView.setText(Stats);
                              } /*if*/
                          } /*run*/
                      } /*Runnable*/
                  );
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
                this.ViewWidth = ViewWidth;
                this.ViewHeight = ViewHeight;
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
        final Animations NewAnimation
      )
      {
        Render.Synchronize
          (
            new Runnable()
              {
                public void run()
                  {
                    if (NewAnimation != Render.CurAnimationChoice)
                      {
                        Render.StopAnimation();
                        Render.CurAnimationChoice = NewAnimation;
                        Render.StartAnimation();
                      } /*if*/
                  } /*run*/
              } /*Runnable*/
          );
      } /*SetAnimation*/

    public Animations GetAnimation()
      {
        return
            Render.CurAnimationChoice; /* assume no race conditions! */
      } /*GetAnimation*/

    @Override
    public void onPause()
      {
        Render.Synchronize
          (
            new Runnable()
              {
                public void run()
                  {
                    Render.StopAnimation();
                  } /*run*/
              } /*Runnable*/
          );
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
        Render.Synchronize
          (
            new Runnable()
              {
                public void run()
                  {
                    if (Render.CurAnimationChoice != null)
                      {
                        MyState.putInt("AnimationName", Render.CurAnimationChoice.NameID);
                        MyState.putDouble
                          (
                            "DrawTime",
                            Render.CurAnimation != null ?
                                Render.CurAnimation.GetDrawTime()
                            :
                                Render.SavedDrawTime
                          );
                      } /*if*/
                  } /*run*/
              } /*Runnable*/
          );
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
        Render.SavedDrawTime = MyState.getDouble("DrawTime", -1.0);
        final int AnimationName = MyState.getInt("AnimationName", 0);
        if (AnimationName != 0)
          {
            SetAnimation(Animations.WithName(AnimationName));
          } /*if*/
      } /*onRestoreInstanceState*/

  } /*OnScreenView*/;
