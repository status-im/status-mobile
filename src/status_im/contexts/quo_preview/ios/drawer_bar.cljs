(ns status-im.contexts.quo-preview.ios.drawer-bar
  (:require
    [quo.core :as quo]
    [status-im.contexts.quo-preview.preview :as preview]))

(defn view
  []
  [preview/preview-container {}
   [quo/drawer-bar]])
