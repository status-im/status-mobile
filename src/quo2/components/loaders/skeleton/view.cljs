(ns quo2.components.loaders.skeleton.view
  (:require [quo2.theme :as theme]
            [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.loaders.skeleton.style :as style]
            [quo2.components.loaders.skeleton.constants :as constants]))

(defn- skeleton-item
  [index content color]
  [rn/view {:style (style/container content)}
   [rn/view {:style (style/avatar color)}]
   [rn/view {:style style/content-container}
    [rn/view
     {:style (style/content-view
              {:type    (if (= content :list-items) :message :author)
               :index   index
               :content content
               :color   color})}]
    [rn/view
     {:style (style/content-view
              {:type    (if (= content :list-items) :author :message)
               :index   index
               :content content
               :color   color})}]
    (when (= content :notifications)
      [rn/view
       {:style (style/content-view {:type    :message2
                                    :index   index
                                    :content content
                                    :color   color})}])]])

(defn internal-view
  [{:keys [content theme blur? parent-height]}]
  (let [number-of-skeletons (int (Math/floor (/ parent-height
                                                (get-in constants/layout-dimensions [content :height]))))
        color               (cond
                              blur?           colors/white-opa-5
                              (= theme :dark) colors/neutral-90
                              :else           colors/neutral-5)]
    [rn/view {:style {:padding 8}}
     (doall
      (for [index (range number-of-skeletons)]
        ^{:key index}
        [skeleton-item (mod index 4) content color]))]))

(def view (theme/with-theme internal-view))
