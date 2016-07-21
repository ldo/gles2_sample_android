package nz.gen.geek_central.GLUseful;
/*
    Quaternion representation of 3D rotation transformations.
    All angles are in radians.

    Copyright 2011-2016 by Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.

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

public class Rotation implements android.os.Parcelable
  {
    public final float c, s; /* cosine and sine of half the rotation angle */
    public final float x, y, z; /* rotation axis unit vector */
  /* actually quaternion is (c, s * x, s * y, s * z) */

    public Rotation
      (
        float angle,
        float x,
        float y,
        float z
      )
      /* constructs a Rotation that rotates by the specified angle
        about the axis direction (x, y, z). */
      {
        c = (float)Math.cos(angle / 2);
        s = (float)Math.sin(angle / 2);
        final float mag = (float)Math.sqrt(x * x + y * y + z * z); /* mustn't be zero! */
        this.x = x / mag;
        this.y = y / mag;
        this.z = z / mag;
      } /*Rotation*/

    public Rotation
      (
        float angle,
        Vec3f axis
      )
      {
        this(angle, axis.x, axis.y, axis.z);
      } /*Rotation*/

    public static final Rotation identity = new Rotation(0, 1, 1, 0, 0);
      /* represents no rotation at all */

    private Rotation
      (
        float c,
        float x,
        float y,
        float z,
        Object dummy
      )
      /* internal-use constructor with directly-computed components. Note
        this does not compensate for accumulated rounding errors. */
      {
        this.c = c;
        this.s = (float)Math.sqrt(x * x + y * y + z * z);
        this.x = x / this.s;
        this.y = y / this.s;
        this.z = z / this.s;
      } /*Rotation*/

    private Rotation
      (
        float c,
        float s,
        float x,
        float y,
        float z
      )
      /* internal-use constructor with directly-computed components. Note
        this does not compensate for accumulated rounding errors. */
      {
        this.c = c;
        this.s = s;
        this.x = x;
        this.y = y;
        this.z = z;
      } /*Rotation*/

    public static final android.os.Parcelable.Creator<Rotation> CREATOR =
      /* restore state from a Parcel. */
        new android.os.Parcelable.Creator<Rotation>()
          {
            public Rotation createFromParcel
              (
                android.os.Parcel Post
              )
              {
                final android.os.Bundle MyState = Post.readBundle();
                return
                    new Rotation
                      (
                        MyState.getFloat("c", identity.c),
                        MyState.getFloat("s", identity.s),
                        MyState.getFloat("x", identity.x),
                        MyState.getFloat("y", identity.y),
                        MyState.getFloat("z", identity.z)
                      );
              } /*createFromParcel*/

            public Rotation[] newArray
              (
                int NrElts
              )
              {
                return
                    new Rotation[NrElts];
              } /*newArray*/
          } /*Parcelable.Creator*/;

    @Override
    public int describeContents()
      {
        return
            0; /* nothing special */
      } /*describeContents*/

    @Override
    public void writeToParcel
      (
        android.os.Parcel Post,
        int Flags
      )
      /* save state to a Parcel. */
      {
        final android.os.Bundle MyState = new android.os.Bundle();
        MyState.putFloat("c", c);
        MyState.putFloat("s", s);
        MyState.putFloat("x", x);
        MyState.putFloat("y", y);
        MyState.putFloat("z", z);
        Post.writeBundle(MyState);
      } /*writeToParcel*/

    public Rotation inv()
      /* returns rotation by the opposite angle around the same axis. Or alternatively,
        the same angle around the opposite-pointing axis . */
      {
        return
            new Rotation(c, -s, x, y, z);
      } /*inv*/

    public Rotation mul
      (
        Rotation that
      )
      /* returns composition with another rotation. */
      {
        final float s2 = this.s * that.s;
        return
            new Rotation
              (
                this.c * that.c - (this.x * that.x + this.y * that.y + this.z * that.z) * s2,
                (this.y * that.z - this.z * that.y) * s2 + this.c * that.x * that.s + that.c * this.x * this.s,
                (this.z * that.x - this.x * that.z) * s2 + this.c * that.y * that.s + that.c * this.y * this.s,
                (this.x * that.y - this.y * that.x) * s2 + this.c * that.z * that.s + that.c * this.z * this.s,
                null
              );
      } /*mul*/

    public Rotation div
      (
        Rotation that
      )
      /* returns the difference from another rotation. */
      {
        return
            mul(that.inv());
      } /*div*/

    public Rotation mul
      (
        float frac /* can also be negative or > 1 */
      )
      /* returns the specified fraction of the rotation. */
      {
        return
            new Rotation
              (
                GetAngle() * frac, x, y, z
              );
      } /*mul*/

    public float GetAngle()
      /* returns the rotation angle. */
      {
        return
            2 * (float)Math.atan2(s, c);
      } /*GetAngle*/

    public Vec3f GetAxis()
      {
        return
            new Vec3f(x, y, z);
      } /*GetAxis*/

    public Mat4f GetMatrix()
      /* returns a matrix that performs the rotation transformation. */
      {
        return
            Mat4f.rotation(GetAxis(), GetAngle());
      } /*Apply*/

    public String toString()
      {
        return
            String.format
              (
                GLUseful.StdLocale,
                "Rotation(%e, %e, %e, %e, %e)",
                c, s, x, y, z
              );
      } /*toString*/

  } /*Rotation*/;
