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

	private static Bitmap textAsBitmap(String text, float textSize,
			int textColor) {
		Paint paint = new Paint();
		paint.setTextSize(textSize);
		paint.setColor(textColor);
		paint.setTextAlign(Paint.Align.LEFT);
		int width = (int) (paint.measureText(text) + 0.5f); // round
		float baseline = (int) (-paint.ascent() + 0.5f); // ascent() is negative
		int height = (int) (baseline + paint.descent() + 0.5f);
		Bitmap image = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(image);
		canvas.drawText(text, 0, baseline, paint);

		return image;
	}

	public static Texture createTextureWithText(String aText, float aSize,
			int aColor) {

		Bitmap bitMap = textAsBitmap(aText, aSize, aColor);

		int[] data = new int[bitMap.getWidth() * bitMap.getHeight()];
		bitMap.getPixels(data, 0, bitMap.getWidth(), 0, 0, bitMap.getWidth(),
				bitMap.getHeight());

		return loadTextureFromIntBuffer(data, bitMap.getWidth(),
				bitMap.getHeight());
	}

	// public static Texture createTextureWithText(String text, float aSize,
	// int aColor) {
	public static Texture createTextureWithText2(String text) {
		Log.i(LOGTAG, "!!! createTextureWithText2 start");

		DisplayMetrics metrics = new DisplayMetrics();
		float m_display_density = metrics.density;

		int text_size = (int) (14 * m_display_density);
		int padding = (int) (5 * m_display_density);

		Paint paint = new Paint();
		paint.setColor(0xFFFFFFFF);
		paint.setTextSize(text_size);
		paint.setAntiAlias(true);
		paint.setTypeface(Typeface.DEFAULT_BOLD);

		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);

		if (bounds.width() <= 0 || bounds.height() <= 0) {
			Log.i(LOGTAG, "!!! createTextureWithText2 return err 1");
			return null;
		}

		Bitmap bmp = Bitmap.createBitmap(bounds.width(), bounds.height() + 2
				* padding, Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);

		// Typeface chops = Typeface.createFromAsset(getAssets(),
		// "ChopinScript.ttf");
		// canvas.drawARGB(255, 255, 0, 0);
		canvas.drawText(text, 0, text_size + padding, paint);

		Texture tex = createTextureFromBitmap(bmp);
		tex.mWidth = bounds.width();
		tex.mHeight = bounds.height() + 2 * padding;

		Log.i(LOGTAG, "!!! createTextureWithText2 finish");
		return tex;
	}

	public static Texture createTextureFromBitmap(Bitmap bmp) {
		Log.i(LOGTAG, "!!! createTextureFromBitmap start");
		GL10 gl = (GL10) SampleApplicationGLView.mContextFactory.getGLContext();

		if (gl == null) {
			Log.i(LOGTAG, "!!! createTextureFromBitmap return err 1");
			return null;
		}

		int tex_width = bmp.getWidth();
		int tex_height = bmp.getHeight();

		IntBuffer ib;

		Texture tex;

		try {
			ib = IntBuffer.allocate(tex_width * tex_height);
			bmp.getPixels(ib.array(), 0, tex_width, 0, 0, tex_width, tex_height);
			tex = loadTexture(gl, ib, tex_width, tex_height);
		} catch (OutOfMemoryError ex) {
			Log.i(LOGTAG, "!!! createTextureFromBitmap catch err2");
			bmp.recycle();
			return null;
		}

		bmp.recycle();
		Log.i(LOGTAG, "!!! createTextureFromBitmap finish");
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

		// tex.mWidth = img_width;
		// tex.mHeight = img_height;

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
