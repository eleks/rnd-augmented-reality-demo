/*==============================================================================
 Copyright (c) 2012-2013 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.ar.vuforiatemplate.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.DisplayMetrics;
import android.util.Log;

// Support class for the Vuforia samples applications.
// Exposes functionality for loading a texture from the APK.
public class Texture {
	private static final String LOGTAG = "Vuforia_Texture";

	public int mWidth; // The width of the texture.
	public int mHeight; // The height of the texture.
	public int mChannels; // The number of channels.
	public ByteBuffer mData; // The pixel data.
	public int[] mTextureID = new int[1];
	public boolean mSuccess = false;
	public int mGLTextureType = GLES20.GL_TEXTURE_2D;

	/* Factory function to load a texture from the APK. */
	public static Texture loadTextureFromApk(String fileName,
			AssetManager assets) {
		InputStream inputStream = null;
		try {
			inputStream = assets.open(fileName, AssetManager.ACCESS_BUFFER);

			BufferedInputStream bufferedStream = new BufferedInputStream(
					inputStream);
			Bitmap bitMap = BitmapFactory.decodeStream(bufferedStream);

			int[] data = new int[bitMap.getWidth() * bitMap.getHeight()];
			bitMap.getPixels(data, 0, bitMap.getWidth(), 0, 0,
					bitMap.getWidth(), bitMap.getHeight());

			return loadTextureFromIntBuffer(data, bitMap.getWidth(),
					bitMap.getHeight());
		} catch (IOException e) {
			Log.e(LOGTAG, "Failed to log texture '" + fileName + "' from APK");
			Log.i(LOGTAG, e.getMessage());
			return null;
		}
	}

	public static Texture loadTextureFromIntBuffer(int[] data, int width,
			int height) {
		// Convert:
		int numPixels = width * height;
		byte[] dataBytes = new byte[numPixels * 4];

		for (int p = 0; p < numPixels; ++p) {
			int colour = data[p];
			dataBytes[p * 4] = (byte) (colour >>> 16); // R
			dataBytes[p * 4 + 1] = (byte) (colour >>> 8); // G
			dataBytes[p * 4 + 2] = (byte) colour; // B
			dataBytes[p * 4 + 3] = (byte) (colour >>> 24); // A
		}

		Texture texture = new Texture();
		texture.mWidth = width;
		texture.mHeight = height;
		texture.mChannels = 4;

		texture.mData = ByteBuffer.allocateDirect(dataBytes.length).order(
				ByteOrder.nativeOrder());
		int rowSize = texture.mWidth * texture.mChannels;
		for (int r = 0; r < texture.mHeight; r++)
			texture.mData.put(dataBytes, rowSize * (texture.mHeight - 1 - r),
					rowSize);

		texture.mData.rewind();

		// Cleans variables
		dataBytes = null;
		data = null;

		texture.mSuccess = true;
		return texture;
	}

	private static int powOf2(int a) {
		int r = 2;
		while (a > r)
			r *= 2;
		return r;
	}

	public static Texture createTextureWithText(GL10 gl, String aText,
			float aSize, int aColor) {
		// Prepare text renderer
		Paint textPaint = new Paint();
		textPaint.setTextSize(aSize);
		textPaint.setAntiAlias(true);
		textPaint.setColor(aColor);
		textPaint.setTextAlign(Paint.Align.LEFT);

		int width = (int) (textPaint.measureText(aText) + 0.5f);
		float baseline = (int) (-textPaint.ascent() + 0.5f);
		int height = (int) (baseline + textPaint.descent() + 0.5f);

		int texWidth = powOf2(width);
		int texHeight = powOf2(height);

		int dX = (texWidth - width) / 2;

		// Create an empty, mutable bitmap
		Bitmap bitmap = Bitmap.createBitmap(texWidth, texHeight,
				Bitmap.Config.ARGB_4444);
		// get a canvas to paint over the bitmap
		Canvas canvas = new Canvas(bitmap);
		bitmap.eraseColor(0);

		// draw the text
		canvas.scale(-1.0f, 1.0f);
		canvas.drawText(aText, -width - dX, baseline, textPaint);

		Texture tex = new Texture();

		gl.glGenTextures(1, tex.mTextureID, 0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, tex.mTextureID[0]);

		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR);

		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
				GL10.GL_REPEAT);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
				GL10.GL_REPEAT);

		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

		bitmap.recycle();

		return tex;
	}

	static public Texture loadTexture(GL10 gl, IntBuffer ib, int img_width,
			int img_height) {
		Log.i(LOGTAG, "!!! loadTexture start");

		int id;
		{
			int[] temp = new int[1];
			gl.glGenTextures(1, temp, 0);
			id = temp[0];
		}

		gl.glActiveTexture(GL10.GL_TEXTURE0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, id);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
				GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
				GL10.GL_CLAMP_TO_EDGE);

		int min_size = 1;

		int tex_width = min_size;
		int tex_height = min_size;

		while (tex_width < img_width)
			tex_width *= 2;
		while (tex_height < img_height)
			tex_height *= 2;

		IntBuffer new_ib = IntBuffer.allocate(tex_width * tex_height);

		int color = 0;

		for (int y = 0; y < img_height; y++) {
			for (int x = 0; x < img_width; x++) {
				color = ib.get(y * img_width + x);
				int a = (color >>> 24) & 255;
				int r = (color >>> 16) & 255;
				int g = (color >>> 8) & 255;
				int b = (color & 255);
				color = (a << 24) | (b << 16) | (g << 8) | r;
				new_ib.put(y * tex_width + x, color);
			}
		}

		gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, tex_width,
				tex_height, 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, new_ib);

		Texture tex = new Texture();
		tex.mTextureID[0] = id;

		tex.mWidth = tex_width;
		tex.mHeight = tex_height;

		tex.mChannels = 4;
		tex.mSuccess = true;
		// tex.mData
		// FIXME: mData initialize

		Log.i(LOGTAG, "!!! loadTexture finish");
		return tex;
	}

}
