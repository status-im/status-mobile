(ns status-im.ui.screens.browser.empty-tab.views
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [utils.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.browser.accounts :as accounts]
            [status-im.ui.screens.browser.empty-tab.styles :as styles]
            [status-im.ui.screens.browser.views :as browser]
            [status-im.ui.screens.wallet.components.views :as components]
            [status-im.utils.http :as http])
  (:require-macros [status-im.utils.views :as views]))

(defn hide-sheet-and-dispatch
  [event]
  (re-frame/dispatch [:bottom-sheet/hide-old])
  (js/setTimeout #(re-frame/dispatch event) 200))

(defn list-item
  [_]
  (let [loaded (reagent/atom nil)]
    (fn [{:keys [name url] :as bookmark}]
      [quo/list-item
       {:accessibility-label (keyword (str "fav-item" name))
        :on-press            #(re-frame/dispatch [:browser.ui/open-url url])
        :on-long-press       (fn []
                               (re-frame/dispatch
                                [:bottom-sheet/show-sheet-old
                                 {:content (fn []
                                             [react/view {:flex 1}
                                              [quo/list-item
                                               {:theme               :accent
                                                :title               (i18n/label :t/open-in-new-tab)
                                                :accessibility-label :open-in-new-tab
                                                :icon                :main-icons/tabs
                                                :on-press            #(hide-sheet-and-dispatch
                                                                       [:browser.ui/open-url url])}]
                                              [quo/list-item
                                               {:theme               :accent
                                                :title               (i18n/label :t/edit)
                                                :accessibility-label :edit-bookmark
                                                :icon                :main-icons/edit
                                                :on-press            #(hide-sheet-and-dispatch
                                                                       [:open-modal :new-bookmark
                                                                        bookmark])}]
                                              [quo/list-item
                                               {:theme               :negative
                                                :title               (i18n/label :t/delete)
                                                :accessibility-label :delete-bookmark
                                                :icon                :main-icons/delete
                                                :on-press            #(hide-sheet-and-dispatch
                                                                       [:browser/delete-bookmark
                                                                        url])}]])}]))
        :title               name
        :subtitle            (or url (i18n/label :t/dapp))
        :icon                [react/view
                              {:width           40
                               :height          40
                               :align-items     :center
                               :justify-content :center}
                              (when (or (nil? @loaded) @loaded)
                                [react/image
                                 {:onLoad #(reset! loaded true)
                                  :style  {:width 32 :height 32 :position :absolute :top 4 :left 4}
                                  :source {:uri (str "https://" (http/url-host url) "/favicon.ico")}}])
                              (when-not @loaded
                                [react/view
                                 {:width            40
                                  :height           40
                                  :border-radius    20
                                  :background-color colors/gray-lighter
                                  :align-items      :center
                                  :justify-content  :center}
                                 [icons/icon :main-icons/browser {:color colors/gray}]])]}])))

(def dapp-image-data {:image (resources/get-image :dapp-store) :width 768 :height 333})
(defn dapp-image [] [components.common/image-contain nil dapp-image-data])

(defn list-header
  [empty-bookmarks?]
  [react/view
   [react/touchable-highlight {:on-press #(re-frame/dispatch [:browser.ui/open-url "https://dap.ps"])}
    [react/view (styles/dapp-store-container)
     [dapp-image nil dapp-image-data]
     [react/text {:style styles/open-dapp-store} (i18n/label :t/open-dapp-store)]
     [react/text {:style {:color colors/blue :font-size 13 :line-height 22}} "https://dap.ps ->"]]]
   (when-not empty-bookmarks?
     [react/view {:margin-top 14 :margin-left 16 :margin-bottom 4}
      [react/text {:style {:line-height 22 :font-size 15 :color colors/gray}}
       (i18n/label :t/favourites)]])])

(views/defview select-account
  []
  (views/letsubs [accounts                               [:accounts-without-watch-only]
                  {:keys [name color] :as dapps-account} [:dapps-account]]
    [react/view
     {:position           :absolute
      :z-index            2
      :align-items        :center
      :bottom             16
      :left               0
      :right              0
      :padding-horizontal 32}
     [quo/button
      {:accessibility-label :select-account
       :type                :scale
       :on-press            #(re-frame/dispatch [:bottom-sheet/show-sheet-old
                                                 {:content (accounts/accounts-list accounts
                                                                                   dapps-account)}])}
      [react/view (styles/dapps-account color)
       [icons/icon :main-icons/account {:color colors/white-persist}]
       [react/view {:flex-shrink 1}
        [react/text
         {:numberOfLines 1
          :style         {:margin-horizontal 6
                          :color             colors/white-persist
                          :typography        :main-medium}}
         name]]
       [icons/icon :main-icons/dropdown {:color colors/white-transparent-persist}]]]]))

(views/defview empty-tab
  []
  (views/letsubs [bookmarks     [:bookmarks/active]
                  dapps-account [:dapps-account]
                  url-text      (atom nil)]
    (let [bookmarks (vals bookmarks)]
      [react/keyboard-avoiding-view
       {:style         {:flex 1}
        :ignore-offset true}
       [quo/text-input
        {:on-change-text      #(reset! url-text %)
         :on-submit-editing   #(re-frame/dispatch [:browser.ui/open-url @url-text])
         :placeholder         (i18n/label :t/enter-url)
         :auto-capitalize     :none
         :auto-correct        false
         :style               styles/input
         :container-style     styles/input-container-style
         :accessibility-label :dapp-url-input
         :return-key-type     :go}]
       [components/separator-dark]
       [list/flat-list
        {:header          [list-header (empty? bookmarks)]
         :data            bookmarks
         :key-fn          :browser-id
         :empty-component [react/view {:align-items :center :margin-top 20}
                           [icons/icon :main-icons/favourite {:color colors/gray}]
                           [react/text {:style {:color colors/gray :margin-top 4}}
                            (i18n/label :t/favourite-description)]]
         :render-fn       list-item}]
       [browser/navigation
        {:dapps-account dapps-account
         :empty-tab     true}]])))
