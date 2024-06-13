(ns status-im.contexts.settings.wallet.saved-addresses.view
  (:require
    [clojure.string :as string]
    [oops.core :as oops]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [status-im.common.resources :as resources]
    [status-im.contexts.settings.wallet.saved-addresses.sheets.address-options.view :as address-options]
    [status-im.contexts.settings.wallet.saved-addresses.style :as style]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- empty-list
  []
  (let [theme (quo.theme/use-theme)]
    [quo/empty-state
     {:title           (i18n/label :t/no-saved-addresses)
      :description     (i18n/label :t/you-like-to-type-43-characters)
      :image           (resources/get-themed-image :sweating-man theme)
      :container-style style/empty-container-style}]))

(defn- empty-result
  []
  (let [theme (quo.theme/use-theme)]
    [quo/empty-state
     {:title           (i18n/label :t/nothing-found)
      :description     (i18n/label :t/try-to-search-something-else)
      :image           (resources/get-themed-image :no-contacts theme)
      :container-style style/empty-container-style}]))

(defn- saved-address
  [{:keys [name address chain-short-names customization-color ens? ens network-preferences-names]}]
  (let [full-address           (str chain-short-names address)
        on-press-saved-address (rn/use-callback
                                #(rf/dispatch
                                  [:show-bottom-sheet
                                   {:theme           :dark
                                    :shell?          true
                                    :blur-background colors/bottom-sheet-background-blur
                                    :content         (fn []
                                                       [address-options/view
                                                        {:address address
                                                         :chain-short-names chain-short-names
                                                         :full-address full-address
                                                         :name name
                                                         :network-preferences-names
                                                         network-preferences-names
                                                         :customization-color customization-color}])}])
                                [address chain-short-names full-address name customization-color])]
    [quo/saved-address
     {:blur?           true
      :user-props      {:name                name
                        :address             full-address
                        :ens                 (when ens? ens)
                        :customization-color customization-color
                        :blur?               true}
      :container-style {:margin-horizontal 8}
      :on-press        on-press-saved-address}]))

(defn- header
  [{:keys [title index]}]
  [quo/divider-label
   {:tight?          true
    :blur?           true
    :container-style (when (pos? index) {:margin-top 8})}
   title])

(defn- filtered-list
  [{:keys [search-text]}]
  [rn/flat-list
   {:key-fn                          :address
    :data                            (rf/sub [:wallet/filtered-saved-addresses search-text])
    :render-fn                       saved-address
    :shows-vertical-scroll-indicator false
    :keyboard-should-persist-taps    :always
    :content-container-style         {:flex-grow 1}
    :empty-component                 [empty-result]}])

(defn- unfiltered-list
  [{:keys [grouped-saved-addresses]}]
  [rn/section-list
   {:key-fn                          :title
    :shows-vertical-scroll-indicator false
    :sticky-section-headers-enabled  false
    :keyboard-should-persist-taps    :always
    :render-section-header-fn        header
    :sections                        grouped-saved-addresses
    :render-fn                       saved-address
    :content-container-style         {:flex-grow 1}
    :empty-component                 [empty-list]}])

(defn- navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn- add-address-to-save
  []
  (rf/dispatch [:open-modal :screen/settings.add-address-to-save]))

(defn view
  []
  (let [alert-banners-top-margin      (rf/sub [:alert-banners/top-margin])
        customization-color           (rf/sub [:profile/customization-color])
        has-saved-addresses?          (rf/sub [:wallet/has-saved-addresses?])
        grouped-saved-addresses       (rf/sub [:wallet/grouped-saved-addresses])
        input-ref                     (rn/use-ref-atom nil)
        [search-text set-search-text] (rn/use-state "")
        set-input-ref                 (rn/use-callback #(reset! input-ref %))
        on-clear-input                (rn/use-callback
                                       (fn []
                                         (some-> @input-ref
                                                 (oops/ocall "clear"))
                                         (set-search-text "")))
        on-change-text                (rn/use-callback
                                       (debounce/debounce
                                        #(set-search-text %)
                                        500))
        search-active?                (not (string/blank? search-text))
        page-top-props                (rn/use-memo
                                       #(cond-> {:title               (i18n/label :t/saved-addresses)
                                                 :accessibility-label :saved-addresses-header
                                                 :title-right         :action
                                                 :title-right-props   {:icon :i/add
                                                                       :customization-color
                                                                       customization-color
                                                                       :on-press add-address-to-save}
                                                 :blur?               true}

                                          has-saved-addresses?
                                          (assoc
                                           :input       :search
                                           :input-props {:placeholder         (i18n/label
                                                                               :t/name-ens-or-address)
                                                         :ref                 set-input-ref
                                                         :on-change-text      on-change-text
                                                         :show-clear-button?  search-active?
                                                         :on-clear            on-clear-input
                                                         :customization-color customization-color}))
                                       [has-saved-addresses? customization-color search-text])]
    [quo/overlay
     {:type       :shell
      :top-inset? true}
     [quo/page-nav
      {:key        :header
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   navigate-back}]
     [quo/page-top page-top-props]
     [rn/keyboard-avoiding-view
      {:style                    {:flex 1}
       :keyboard-vertical-offset (if platform/ios? alert-banners-top-margin 0)}
      (if search-active?
        [filtered-list {:search-text search-text}]
        [unfiltered-list {:grouped-saved-addresses grouped-saved-addresses}])]]))
