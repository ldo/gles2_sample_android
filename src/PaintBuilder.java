package nz.gen.geek_central.GraphicsUseful;
/*
    Convenient construction of Paint objects by chaining setup calls. E.g.

        Paint MyPaint = new PaintBuilder(true)
            .setColor(SomeColor)
            .setTextSize(SomeTextSize)
            ... other settings ...
            .get();

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

import android.graphics.Paint;

public class PaintBuilder
  {
    public final Paint ThePaint;

    public PaintBuilder
     (
        boolean AntiAlias
     )
      {
        ThePaint = new Paint();
        if (AntiAlias)
          {
            ThePaint.setAntiAlias(true);
            ThePaint.setFilterBitmap(true);
          } /*if*/
      } /*PaintBuilder*/

    public Paint get()
      /* can end a chain set of setup calls with this to get back actual Paint
        object and discard the PaintBuilder. */
      {
        return
            ThePaint;
      } /*get*/

/*
    All following calls correspond directly to Paint calls with the same names,
    they just return the same PaintBuilder object to allow convenient chaining
    of further calls.
*/

    public PaintBuilder reset()
      {
        ThePaint.reset();
        return
            this;
      } /*reset*/

    public PaintBuilder set
      (
        Paint src
      )
      {
        ThePaint.set(src);
        return
            this;
      } /*set*/

    public PaintBuilder setARGB
      (
        int a,
        int r,
        int g,
        int b
      )
      {
        ThePaint.setARGB(a, r, g, b);
        return
            this;
      } /*setARGB*/

    public PaintBuilder setAlpha
      (
        int a
      )
      {
        ThePaint.setAlpha(a);
        return
            this;
      } /*setAlpha*/

    public PaintBuilder setAntiAlias
      (
        boolean aa
      )
      {
        ThePaint.setAntiAlias(aa);
        return
            this;
      } /*setAntiAlias*/

    public PaintBuilder setColor
      (
        int color
      )
      {
        ThePaint.setColor(color);
        return
            this;
      } /*setColor*/

    public PaintBuilder setColorFilter
      (
        android.graphics.ColorFilter filter
      )
      {
        ThePaint.setColorFilter(filter);
        return
            this;
      } /*setColorFilter*/

    public PaintBuilder setDither
      (
        boolean dither
      )
      {
        ThePaint.setDither(dither);
        return
            this;
      } /*setDither*/

    public PaintBuilder setFakeBoldText
      (
        boolean fakeBoldText
      )
      {
        ThePaint.setFakeBoldText(fakeBoldText);
        return
            this;
      } /*setFakeBoldText*/

    public PaintBuilder setFilterBitmap
      (
        boolean filterBitmap
      )
      {
        ThePaint.setFilterBitmap(filterBitmap);
        return
            this;
      } /*setFilterBitmap*/

    public PaintBuilder setFlags
      (
        int flags
      )
      {
        ThePaint.setFlags(flags);
        return
            this;
      } /*setFlags*/

  /* setHinting only available in API 14 and later */
  /* public PaintBuilder setHinting
      (
        int mode
      )
      {
        ThePaint.setHinting(mode);
        return
            this;
      } /*setHinting*/

    public PaintBuilder setLinearText
      (
        boolean linearText
      )
      {
        ThePaint.setLinearText(linearText);
        return
            this;
      } /*setLinearText*/

    public PaintBuilder setMaskFilter
      (
        android.graphics.MaskFilter maskfilter
      )
      {
        ThePaint.setMaskFilter(maskfilter);
        return
            this;
      } /*setMaskFilter*/

    public PaintBuilder setPathEffect
      (
        android.graphics.PathEffect effect
      )
      {
        ThePaint.setPathEffect(effect);
        return
            this;
      } /*setPathEffect*/

    public PaintBuilder setRasterizer
      (
        android.graphics.Rasterizer rasterizer
      )
      {
        ThePaint.setRasterizer(rasterizer);
        return
            this;
      } /*setRasterizer*/

    public PaintBuilder setShader
      (
        android.graphics.Shader shader
      )
      {
        ThePaint.setShader(shader);
        return
            this;
      } /*setShader*/

    public PaintBuilder setShadowLayer
      (
        float radius,
        float dx,
        float dy,
        int color
      )
      {
        ThePaint.setShadowLayer(radius, dx, dy, color);
        return
            this;
      } /*setShadowLayer*/

    public PaintBuilder setStrikeThruText
      (
        boolean strikeThruText
      )
      {
        ThePaint.setStrikeThruText(strikeThruText);
        return
            this;
      } /*setStrikeThruText*/

    public PaintBuilder setStrokeCap
      (
        Paint.Cap cap
      )
      {
        ThePaint.setStrokeCap(cap);
        return
            this;
      } /*setStrokeCap*/

    public PaintBuilder setStrokeJoin
      (
        Paint.Join join
      )
      {
        ThePaint.setStrokeJoin(join);
        return
            this;
      } /*setStrokeJoin*/

    public PaintBuilder setStrokeMiter
      (
        float miter
      )
      {
        ThePaint.setStrokeMiter(miter);
        return
            this;
      } /*setStrokeMiter*/

    public PaintBuilder setStrokeWidth
      (
        float width
      )
      {
        ThePaint.setStrokeWidth(width);
        return
            this;
      } /*setStrokeWidth*/

    public PaintBuilder setStyle
      (
        Paint.Style style
      )
      {
        ThePaint.setStyle(style);
        return
            this;
      } /*setStyle*/

    public PaintBuilder setSubpixelText
      (
        boolean subpixelText
      )
      {
        ThePaint.setSubpixelText(subpixelText);
        return
            this;
      } /*setSubpixelText*/

    public PaintBuilder setTextAlign
      (
        Paint.Align align
      )
      {
        ThePaint.setTextAlign(align);
        return
            this;
      } /*setTextAlign*/

    public PaintBuilder setTextScaleX
      (
        float scaleX
      )
      {
        ThePaint.setTextScaleX(scaleX);
        return
            this;
      } /*setTextScaleX*/

    public PaintBuilder setTextSize
      (
        float textSize
      )
      {
        ThePaint.setTextSize(textSize);
        return
            this;
      } /*setTextSize*/

    public PaintBuilder setTextSkewX
      (
        float skewX
      )
      {
        ThePaint.setTextSkewX(skewX);
        return
            this;
      } /*setTextSkewX*/

    public PaintBuilder setTypeface
      (
        android.graphics.Typeface typeface
      )
      {
        ThePaint.setTypeface(typeface);
        return
            this;
      } /*setTypeface*/

    public PaintBuilder setUnderlineText
      (
        boolean underlineText
      )
      {
        ThePaint.setUnderlineText(underlineText);
        return
            this;
      } /*setUnderlineText*/

    public PaintBuilder setXfermode
      (
        android.graphics.Xfermode xfermode
      )
      {
        ThePaint.setXfermode(xfermode);
        return
            this;
      } /*setXfermode*/

  } /*PaintBuilder*/
