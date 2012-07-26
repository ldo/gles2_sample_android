package nz.gen.geek_central.android.useful;
/*
    Generic class for easier implementation of save/restore instance state
    in a custom View subclass. This class holds all the state as items in
    a Bundle.

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

import android.os.Parcelable;
import android.os.Parcel;
import android.os.Bundle;
import android.view.AbsSavedState;

public class BundledSavedState extends AbsSavedState
  {

    public static Parcelable.Creator<BundledSavedState> CREATOR =
        new Parcelable.Creator<BundledSavedState>()
          {
            public BundledSavedState createFromParcel
              (
                Parcel SavedState
              )
              {
                final AbsSavedState SuperState =
                    AbsSavedState.CREATOR.createFromParcel(SavedState);
                final Bundle MyState = SavedState.readBundle();
                return
                    new BundledSavedState(SuperState, MyState);
              } /*createFromParcel*/

            public BundledSavedState[] newArray
              (
                int NrElts
              )
              {
                return
                    new BundledSavedState[NrElts];
              } /*newArray*/
          } /*Parcelable.Creator*/;

        public final Parcelable SuperState;
        public final Bundle MyState;

        public BundledSavedState
          (
            Parcelable SuperState,
            Bundle MyState
          )
          {
            super(SuperState);
            this.SuperState = SuperState;
            this.MyState = MyState;
          } /*BundledSavedState*/

        public void writeToParcel
          (
            Parcel SavedState,
            int Flags
          )
          {
            super.writeToParcel(SavedState, Flags);
            SavedState.writeBundle(MyState);
          } /*writeToParcel*/

  } /*BundledSavedState*/
