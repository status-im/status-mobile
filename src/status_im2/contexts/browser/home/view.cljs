(ns status-im2.contexts.browser.home.view
  (:require
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [utils.i18n :as i18n]
    [status-im2.common.home.top-nav.view :as common.top-nav]
    [status-im2.common.home.title-column.view :as common.title-column]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [top                 (safe-area/get-top)
        customization-color (rf/sub [:profile/customization-color])]
    (fn []
      [rn/view
       {:style {:margin-top top
                :flex       1}}
       [common.top-nav/view]
       [common.title-column/view
        {:label (i18n/label :t/browser)
         :customization-color customization-color}]
      ])))
