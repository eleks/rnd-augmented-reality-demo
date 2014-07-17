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

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLES20;
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

	private static Bitmap textAsBitmap(String text, float textSize, int textColor) {
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

}
