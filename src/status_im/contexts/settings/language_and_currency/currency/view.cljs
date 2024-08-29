(ns status-im.contexts.settings.language-and-currency.currency.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.resources :as resources]
            [status-im.constants :as constants]
            [status-im.contexts.settings.language-and-currency.currency.style :as style]
            [status-im.contexts.settings.language-and-currency.currency.utils :as utils]
            [status-im.contexts.settings.language-and-currency.data-store :as data-store]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn- get-item-layout
  [_ index]
  #js {:length constants/currency-item-height
       :offset (* constants/currency-item-height index)
       :index  index})

(defn- settings-category-view
  [{:keys [title data]} _ _ {:keys [selected-currency on-currency-press]}]
  [rn/delay-render
   [quo/category
    {:label     title
     :data      (map (fn [currency]
                       (utils/make-currency-item
                        {:currency          currency
                         :selected-currency selected-currency
                         :on-change         on-currency-press}))
                     data)
     :blur?     true
     :list-type :settings}]])

(defn view
  []
  (let [[search-text set-search-text] (rn/use-state "")
        on-currency-press             (rn/use-callback #(rf/dispatch [:profile.settings/update-currency
                                                                      %]))
        selected-currency             (rf/sub [:profile/currency])
        currencies                    (rf/sub [:currencies/categorized search-text])
        on-change-text                (rn/use-callback
                                       (debounce/debounce
                                        #(set-search-text %)
                                        300))
        formatted-data                (rn/use-memo
                                       #(data-store/get-formatted-currency-data currencies)
                                       [search-text currencies])]
    [quo/overlay
     {:type       :shell
      :top-inset? true}
     [quo/page-nav
      {:background :blur
       :icon-name  :i/arrow-left
       :on-press   navigate-back}]
     [quo/page-top
      {:title (i18n/label :t/currency)}]
     [quo/input
      {:small?          true
       :blur?           true
       :container-style style/input-container
       :placeholder     (i18n/label :t/search-currencies)
       :icon-name       :i/search
       :on-change-text  on-change-text}]
     (when (zero? (:total currencies))
       [quo/empty-state
        {:title           (i18n/label :t/no-result)
         :image           (resources/get-themed-image :no-assets :dark)
         :container-style style/empty-results
         :description     (i18n/label :t/try-with-different-currency)}])
     [rn/flat-list
      {:data                            formatted-data
       :render-data                     {:selected-currency selected-currency
                                         :on-currency-press on-currency-press}
       :render-fn                       settings-category-view
       :get-item-layout                 get-item-layout
       :initial-num-to-render           14
       :max-to-render-per-batch         20
       :scroll-event-throttle           16
       :shows-vertical-scroll-indicator false
       :bounces                         false
       :over-scroll-mode                :never}]]))
