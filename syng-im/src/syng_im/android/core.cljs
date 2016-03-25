(ns syng-im.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.handlers]
            [syng-im.subs]
            [syng-im.components.app-root :refer [app-root]]
            [syng-im.components.react :refer [view text image touchable-highlight]]
            [syng-im.components.react :refer [app-registry]]))


(defn init []
  (dispatch-sync [:initialize-db])
  (dispatch [:initialize-protocol])
  (.registerComponent app-registry "SyngIm" #(r/reactify-component app-root)))
