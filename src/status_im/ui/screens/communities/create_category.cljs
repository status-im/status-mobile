(ns status-im.ui.screens.communities.create-category
  (:require [status-im.ui.components.keyboard-avoid-presentation :as kb-presentation]
            [status-im.utils.handlers :refer [<sub]]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.components.react :as react]
            [quo.core :as quo]
            [status-im.utils.debounce :as debounce]
            [status-im.i18n.i18n :as i18n]
            [clojure.string :as str]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.screens.home.views.inner-item :as inner-item]
            [status-im.communities.core :as communities]
            [reagent.core :as reagent]))

(defn valid? [category-name]
  (and (not (str/blank? category-name))
       (<= (count category-name) 30)))

(def selected-items (reagent/atom #{}))

(defn render-fn [{:keys [chat-id] :as home-item}]
  (let [selected (get @selected-items chat-id)
        on-change (fn []
                    (swap! selected-items #(if selected (disj % chat-id) (conj % chat-id))))]
    [react/view {:flex-direction :row :flex 1 :align-items :center}
     [react/view {:padding-left 16}
      [quo/checkbox {:value     selected
                     :on-change on-change}]]
     [react/view {:flex 1}
      [inner-item/home-list-item
       (assoc home-item :public? true)
       {:on-press on-change}]]]))

(defn view []
  (let [{:keys [community-id]} (<sub [:get-screen-params])
        category-name (reagent/atom "")
        _ (reset! selected-items #{})]
    (fn []
      (let [chats (<sub [:chats/with-empty-category-by-community-id community-id])]
        [kb-presentation/keyboard-avoiding-view {:style {:flex 1}}
         [react/view {:flex 1}
          [react/view {:padding-horizontal 16}
           [quo/text-input
            {:placeholder    (i18n/label :t/category-title)
             :on-change-text #(reset! category-name %)
             :auto-focus     true}]]
          [quo/separator {:style {:margin-vertical 10}}]
          (when (seq chats)
            [:<>
             [quo/list-header (i18n/label :t/include)]
             [list/flat-list
              {:key-fn                       :chat-id
               :content-container-style      {:padding-vertical 8}
               :keyboard-should-persist-taps :always
               :data                         chats
               :render-fn                    render-fn}]])]
         [toolbar/toolbar
          {:show-border? true
           :center
           [quo/button {:disabled (not (valid? @category-name))
                        :type     :secondary
                        :on-press #(debounce/dispatch-and-chill
                                    [::communities/create-category-confirmation-pressed
                                     community-id
                                     @category-name
                                     (vec @selected-items)]
                                    3000)}
            (i18n/label :t/create)]}]]))))
