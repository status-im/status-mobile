(ns status-im.contexts.preview-screens.quo-preview.ios.drawer-bar
  (:require
    [quo.core :as quo]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(defn view
  []
  [preview/preview-container {}
   [quo/drawer-bar]])
