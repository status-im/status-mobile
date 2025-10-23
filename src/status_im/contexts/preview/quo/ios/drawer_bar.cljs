(ns status-im.contexts.preview.quo.ios.drawer-bar
  (:require
    [quo.core :as quo]
    [status-im.contexts.preview.quo.preview :as preview]))

(defn view
  []
  [preview/preview-container {}
   [quo/drawer-bar]])
