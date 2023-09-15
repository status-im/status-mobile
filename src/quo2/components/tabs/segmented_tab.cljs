(ns quo2.components.tabs.segmented-tab
  (:require [quo2.components.tabs.tab.view :as tab]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [quo2.theme :as quo.theme]))

(def themes-for-blur
  {:light {:background-color colors/neutral-80-opa-5}
   :dark  {:background-color colors/white-opa-5}})

(def themes
  {:light {:background-color colors/neutral-10}
   :dark  {:background-color colors/neutral-90}})

(defn- segmented-control-internal
  [{:keys [default-active on-change]}]
  (let [active-tab-id (reagent/atom default-active)]
    (fn [{:keys [data size theme blur? container-style item-container-style
                 active-item-container-style]}]
      (let [active-id @active-tab-id]
        [rn/view
         (merge
          {:flex-direction   :row
           :background-color (get-in (if blur? themes-for-blur themes)
                                     [theme :background-color])
           :border-radius    (case size
                               32 10
                               28 10
                               24 8
                               20 6)
           :padding          2}
          container-style)
         (for [[indx {:keys [label id]}] (map-indexed vector data)]
           ^{:key id}
           [rn/view
            {:margin-left (if (= 0 indx) 0 2)
             :flex        1}
            [tab/view
             {:id                          id
              :active-item-container-style active-item-container-style
              :item-container-style        item-container-style
              :segmented?                  true
              :size                        size
              :blur?                       blur?
              :active                      (= id active-id)
              :on-press                    (fn [tab-id]
                                             (reset! active-tab-id tab-id)
                                             (when on-change
                                               (on-change tab-id)))}
             label]])]))))

(def segmented-control (quo.theme/with-theme segmented-control-internal))
