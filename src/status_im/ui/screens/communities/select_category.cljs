(ns status-im.ui.screens.communities.select-category
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [reagent.core :as reagent]
            [status-im.communities.core :as communities]
            [utils.i18n :as i18n]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.components.topbar :as topbar]
            [utils.re-frame :as rf]
            [utils.debounce :as debounce]))

(def selected-item (reagent/atom ""))

(defn render-fn
  [{:keys [name id]}]
  [quo/list-item
   {:title     name
    :accessory :radio
    :on-press  #(reset! selected-item id)
    :active    (= id @selected-item)
    :icon      [icons/icon :main-icons/channel-category {:color colors/gray}]}])

(defn view
  []
  (let [{:keys [community-id chat]} (rf/sub [:get-screen-params])]
    (fn []
      (let [categories (rf/sub [:communities/sorted-categories community-id])
            chats      (rf/sub [:chats/sorted-categories-by-community-id community-id])
            comm-chat  (rf/sub [:chats/community-chat-by-id community-id (:chat-id chat)])
            _ (reset! selected-item (:categoryID comm-chat))]
        [:<>
         [topbar/topbar
          {:title    (str "#" (:chat-name chat))
           :subtitle (i18n/label :t/public-channel)
           :modal?   true}]
         [react/view {:flex 1}
          [quo/list-header (i18n/label :t/category)]
          [list/flat-list
           {:key-fn                       :chat-id
            :content-container-style      {:padding-vertical 8}
            :keyboard-should-persist-taps :always
            :footer                       [quo/list-item
                                           {:theme    :accent
                                            :icon     :main-icons/channel-category
                                            :on-press #(rf/dispatch [:open-modal
                                                                     :create-community-category
                                                                     {:community-id community-id}])
                                            :title    (i18n/label :t/create-category)}]
            :data                         (conj categories {:name (i18n/label :t/none) :id ""})
            :render-fn                    render-fn}]]
         [toolbar/toolbar
          {:show-border? true
           :center
           [quo/button
            {:type     :secondary
             :on-press #(debounce/dispatch-and-chill
                         [::communities/change-category-confirmation-pressed
                          community-id
                          @selected-item
                          (assoc comm-chat
                                 :position
                                 (count (get chats @selected-item)))] ;; Add as last item in new category
                         3000)}
            (i18n/label :t/done)]}]]))))
