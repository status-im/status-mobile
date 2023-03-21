(ns utils.worklets.bottom-sheet)

(def bottom-sheet-js (js/require "../src/js/worklets/bottom_sheet.js"))

(defn use-translate-y
  [window-height bottom-sheet-dy pan-y]
  (.useTranslateY ^js bottom-sheet-js window-height bottom-sheet-dy pan-y))

(defn use-background-opacity
  [translate-y bg-height window-height]
  (.useBackgroundOpacity ^js bottom-sheet-js translate-y bg-height window-height))
