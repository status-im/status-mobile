(ns quo.components.tags.collectible-tag.view
  (:require
    [oops.core :refer [oget]]
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [quo.components.tags.collectible-tag.style :as style]
    [quo.components.utilities.token.view :as token]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.hole-view :as hole-view]
    [reagent.core :as reagent]))


(defn view-internal [] )

(def view (quo.theme/with-theme view-internal))