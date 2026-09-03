package com.winlator.cmod.runtime.display.renderer;

import com.winlator.cmod.runtime.display.xserver.Drawable;
import com.winlator.cmod.runtime.display.xserver.Window;

class RenderableWindow {
  final Drawable content;
  final Window window;
  short rootX;
  short rootY;

  public RenderableWindow(Window window, Drawable content, int rootX, int rootY) {
    this.window = window;
    this.content = content;
    this.rootX = (short) rootX;
    this.rootY = (short) rootY;
  }
}
