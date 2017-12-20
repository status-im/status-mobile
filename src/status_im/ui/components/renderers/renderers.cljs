(ns status-im.ui.components.renderers.renderers
  (:require [status-im.ui.components.react :refer [list-item]]
            [status-im.ui.components.common.common :as common]))

(defn list-separator-renderer [_ row-id _]
  (list-item
    ^{:key row-id}
    [common/list-separator]))

(defn list-header-renderer [& _]
  (list-item [common/list-header]))

(defn list-footer-renderer [& _]
  (list-item [common/list-footer]))
