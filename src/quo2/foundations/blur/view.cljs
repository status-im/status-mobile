(ns quo2.foundations.blur.view
  (:require [react-native.blur :as blur]
            [quo2.foundations.blur.style :as style]
            [quo2.foundations.colors :as colors]
            [quo2.foundations.shadows :as shadows]
            [react-native.core :as rn]))

(def blur-types
  {:blur-light              {:blur-radius      20
                             :blur-amount      40
                             :blur-type        :light
                             :background-color colors/white-opa-70}
   :blur-dark               {:blur-radius      20
                             :blur-amount      40
                             :blur-type        :dark
                             :background-color colors/neutral-80-opa-80-blur}
   :notification-blur-light {:blur-radius      20
                             :blur-amount      40
                             :blur-type        :light
                             :background-color colors/white-opa-70}
   :notification-blur-dark  {:blur-radius      20
                             :blur-amount      40
                             :blur-type        :dark
                             :background-color colors/neutral-80-opa-80-blur}
   :blur-over-blur-back     {:blur-radius      20
                             :blur-amount      40
                             :blur-type        :light
                             :background-color colors/white-opa-5}
   :blur-over-blur-middle   {:blur-radius      20
                             :blur-amount      40
                             :blur-type        :light
                             :background-color colors/white-opa-10}
   :blur-over-blur-front    {:blur-radius      20
                             :blur-amount      40
                             :blur-type        :light
                             :background-color colors/white-opa-20}})

(def shadow-types
  {:notification-blur-light (shadows/get 1 :light)
   :notification-blur-dark  (shadows/get 1 :dark)})

(defn view
  [{:keys [blur-type container-style] :or {blur-type :blur-light}} children]
  [:<>
   (when (blur-type shadow-types)
     [rn/view
      (merge
       (blur-type shadow-types)
       {:style (style/container container-style)})])

   [blur/view
    (merge
     (blur-type blur-types)
     {:style (style/container container-style)}) children]])
