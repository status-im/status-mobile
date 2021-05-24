(ns status-im.ui.screens.notifications-center.views
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.screens.home.views.inner-item :as inner-item]
            [re-frame.core :as re-frame]
            [quo.core :as quo]
            [status-im.ui.components.colors :as colors]
            [reagent.core :as reagent]
            [status-im.ui.components.toolbar :as toolbar]
            [clojure.string :as string]))

(def selecting (reagent/atom nil))
(def select-all (reagent/atom nil))
(def selected-items (reagent/atom #{}))

(defn render-fn [{:keys [id] :as home-item}]
  (when id
    (let [selected (get @selected-items id)
          on-change (fn []
                      (swap! selected-items #(if selected (disj % id) (conj % id))))]
      [react/view {:flex-direction :row :flex 1 :align-items :center}
       (when @selecting
         [react/view {:padding-left 16}
          [quo/checkbox {:value     (or @select-all selected)
                         :disabled  @select-all
                         :on-change on-change}]])
       [react/view {:flex 1}
        [inner-item/home-list-item
         home-item
         {:on-press      (fn []
                           (if @selecting
                             (on-change)
                             (re-frame/dispatch [:accept-activity-center-notification-and-open-chat id])))
          :on-long-press #(do (reset! selecting true)
                              (swap! selected-items conj id))}]]])))

(defn filter-item []
  [react/view {:padding-vertical 8 :border-bottom-width 1 :border-bottom-color colors/gray-lighter}
   [react/view {:align-items :center :justify-content :space-between :padding-horizontal 16 :flex-direction :row}
    (if @selecting
      [react/view {:flex-direction :row}
       [quo/checkbox {:value     @select-all
                      :on-change #(do
                                    (reset! selected-items #{})
                                    (swap! select-all not))}]
       [react/text {:style {:color colors/gray :margin-left 20}}
        (str (if @select-all (i18n/label :t/all) (count @selected-items))
             " " (string/lower-case (i18n/label :t/selected)))]]
      [quo/button {:type     :secondary
                   :accessibility-label :select-button-activity-center
                   :on-press #(reset! selecting true)}
       (i18n/label :t/select)])
    (when @selecting
      [quo/button {:type     :secondary
                   :on-press #(do (reset! selecting false)
                                  (reset! select-all false)
                                  (reset! selected-items #{}))}
       (i18n/label :t/cancel)])]])

(defn reset-state []
  (reset! selecting nil)
  (reset! select-all nil)
  (reset! selected-items #{}))

(defn toolbar-action [accept]
  (if accept
    (if @select-all
      (re-frame/dispatch [:accept-all-activity-center-notifications])
      (re-frame/dispatch [:accept-activity-center-notifications @selected-items]))
    (if @select-all
      (re-frame/dispatch [:dismiss-all-activity-center-notifications])
      (re-frame/dispatch [:dismiss-activity-center-notifications @selected-items])))
  (reset-state))

(defn center []
  (reagent/create-class
   {:display-name "activity-center"
    :component-did-mount #(re-frame/dispatch [:get-activity-center-notifications])
    :reagent-render (fn []
                      (let [{:keys [notifications]} @(re-frame/subscribe [:activity.center/notifications])]
                        [react/keyboard-avoiding-view {:style {:flex 1}
                                                       :ignore-offset true}
                         [topbar/topbar {:navigation {:on-press #(do
                                                                   (reset-state)
                                                                   (re-frame/dispatch [:close-notifications-center])
                                                                   (re-frame/dispatch [:navigate-back]))}
                                         :title      (i18n/label :t/activity)}]
                         [filter-item]
                         [list/flat-list
                          {:key-fn                       #(or (:chat-id %) (:id %))
                           :on-end-reached               #(re-frame/dispatch [:load-more-activity-center-notifications])
                           :keyboard-should-persist-taps :always
                           :data                         notifications
                           :render-fn                    render-fn}]
                         (when (or @select-all (> (count @selected-items) 0))
                           [toolbar/toolbar
                            {:show-border? true
                             :left         [quo/button {:type     :secondary
                                                        :theme    :negative
                                                        :accessibility-label :reject-and-delete-activity-center
                                                        :on-press #(toolbar-action false)}
                                            (i18n/label :t/reject-and-delete)]
                             :right        [quo/button {:type     :secondary
                                                        :accessibility-label :accept-and-add-activity-center
                                                        :on-press #(toolbar-action true)}
                                            (i18n/label :t/accept-and-add)]}])]))}))
