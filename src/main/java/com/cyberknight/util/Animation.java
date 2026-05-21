package com.cyberknight.util;

import java.awt.image.BufferedImage;

/**
 * Animation – mengontrol urutan frame dari SpriteSheet.
 *
 * Fitur:
 * - Loop / one-shot
 * - Speed per animasi (frame delay)
 * - Callback saat animasi selesai (untuk one-shot)
 */
public class Animation {

    private SpriteSheet sheet;
    private int         currentFrame = 0;
    private int         timer        = 0;
    private int         frameDelay;       // frame game per sprite frame
    private boolean     loop;
    private boolean     finished     = false;
    private Runnable    onFinished;

    public Animation(SpriteSheet sheet, int frameDelay, boolean loop) {
        this.sheet      = sheet;
        this.frameDelay = frameDelay;
        this.loop       = loop;
    }

    public void setOnFinished(Runnable r) { this.onFinished = r; }

    public void update() {
        if (finished && !loop) return;

        timer++;
        if (timer >= frameDelay) {
            timer = 0;
            currentFrame++;
            if (currentFrame >= sheet.getFrameCount()) {
                if (loop) {
                    currentFrame = 0;
                } else {
                    currentFrame = sheet.getFrameCount() - 1;
                    if (!finished) {
                        finished = true;
                        if (onFinished != null) onFinished.run();
                    }
                }
            }
        }
    }

    public void reset() {
        currentFrame = 0;
        timer        = 0;
        finished     = false;
    }

    public BufferedImage getCurrentFrame() {
        return sheet.getFrame(currentFrame);
    }

    public boolean isFinished()  { return finished; }
    public int getFrameIndex()   { return currentFrame; }
    public SpriteSheet getSheet(){ return sheet; }
}