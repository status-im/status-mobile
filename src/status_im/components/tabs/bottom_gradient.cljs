(ns status-im.components.tabs.bottom-gradient
  (:require [status-im.components.tabs.styles :as st]
            [status-im.components.react :refer [linear-gradient]]))

(defn bottom-gradient []
  [linear-gradient {:locations [0 0.8 1]
                    :colors    ["rgba(24, 52, 76, 0)" "rgba(24, 52, 76, 0.085)" "rgba(24, 52, 76, 0.165)"]
                    :style     st/bottom-gradient}])