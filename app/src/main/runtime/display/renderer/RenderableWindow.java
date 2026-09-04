package com.winlator.cmod.runtime.display.renderer;

import com.winlator.cmod.runtime.display.xserver.Drawable;
import com.winlator.cmod.runtime.display.xserver.Window;

class RenderableWindow {
  final Drawable content;
  final Window window;
  final int windowId;
  int rootX;
  int rootY;

  public RenderableWindow(Window window, Drawable content, int rootX, int rootY) {
    this.window = window;
    this.windowId = window != null ? window.getWindowId() : 0;
    this.content = content;
    this.rootX = rootX;
    this.rootY = rootY;
  }
}
