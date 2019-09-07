(ns quo.previews.main
  (:require [oops.core :refer [ocall]]
            [quo.previews.header :as header]
            [quo.previews.text :as text]
            [quo.react-native :as rn]
            [reagent.core :as reagent]
            [status-im.ui.screens.routing.core :as navigation]))

(def screens [{:name      :texts
               :insets    {:top false}
               :component text/preview-text}
              {:name      :headers
               :insets    {:top false}
               :component header/preview-header}])

(defn main-screen []
  [rn/scroll-view {:flex               1
                   :padding-vertical   8
                   :padding-horizontal 16}
   [rn/view
    (for [{:keys [name]} screens]
      [rn/touchable-opacity {:on-press #(navigation/navigate-to name nil)}
       [rn/view {:style {:padding-vertical 8}}
        [rn/text (str "Preview " name)]]])]])

(defonce navigation-state (atom nil))

(defn- persist-state! [state-obj]
  (js/Promise.
   (fn [resolve _]
     (reset! navigation-state state-obj)
     (resolve true))))

(defn preview-screens []
  (let [stack (navigation/create-stack)]
    [navigation/navigation-container
     {:ref             navigation/set-navigator-ref
      :initial-state   @navigation-state
      :on-state-change persist-state!}
     [stack {}
      (into [{:name      :main
              :insets    {:top false}
              :component main-screen}]
            screens)]]))



;; TODO(Ferossgp): Add separate build when shadow-cljs will be integrated
;; NOTE(Ferossgp): Separate app can be used to preview all available
;; and possible state for components, and for UI testing based on screenshots


(defn init []
  (.registerComponent ^js rn/app-registry "StatusIm" #(reagent/reactify-component preview-screens)))
