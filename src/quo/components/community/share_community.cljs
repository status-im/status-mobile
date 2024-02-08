(ns quo.components.community.share-community
  (:require
   [quo.components.share.qr-code.view :as qr-code]
   [quo.core :as quo]
   [quo.theme]
   [react-native.core :as rn]
   [react-native.safe-area :as safe-area]
   [utils.re-frame :as rf]))

(def qr-size 500)

(def header-container
  {:padding-horizontal 20
   :padding-vertical   12})

(defn- view-internal
  []
  (let [padding-top       (:top (safe-area/get-insets))]
    (fn []

      [quo/overlay {:type :shell}
       [rn/view
        {:flex        1
         :padding-top padding-top
         :key         :share-community}
        [quo/page-nav
         {:icon-name           :i/close
          :on-press            #(rf/dispatch [:navigate-back])
          :background          :blur
          :accessibility-label :top-bar}]
        [quo/text-combinations
         {:container-style header-container
          :title           "Share Channel"}]
        [rn/view {:style {:padding-horizontal 20}}
         [qr-code/view {:avatar :community :size qr-size}]]]])))

(def view (quo.theme/with-theme view-internal))



