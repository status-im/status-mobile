(ns status-im.ui.screens.desktop.main.buidl.new-issue.views
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.desktop.main.buidl.views :as buidl.views])
  (:require-macros [status-im.utils.views :as views]))

(defn tag-view [tag {:keys [on-press]}]
  [react/touchable-highlight {:style {:border-radius 5
                                      :margin 2
                                      :padding 4
                                      :height 23
                                      :background-color (if (= tag "clear filter")
                                                          colors/red
                                                          colors/blue-dark)
                                      :justify-content :center
                                      :align-items :center
                                      :align-content :center}
                              :on-press on-press}
   [react/text {:style {:font-size 9
                        :color colors/white}
                :font :medium} tag]])

(views/defview buidl-text-input [field]
  [react/view {:stlye {:flex 1
                       :flex-direction    :row}}
   [react/text {:style {:font-size 18
                        :text-align :center}
                :font :medium}
    (str "#" (string/capitalize (name field)))]
   [react/text-input {:placeholder "Type here..."
                      :auto-focus             true
                      :multiline              true
                      :blur-on-submit         true
                      :style {:color :black
                              :height            200
                              :margin-left       20
                              :margin-right      22}
                      :on-key-press           (fn [e]
                                                (let [native-event (.-nativeEvent e)
                                                      key          (.-key native-event)
                                                      modifiers    (js->clj (.-modifiers native-event))
                                                      should-send  (and (= key "Enter") (not (contains? (set modifiers) "shift")))]
                                                  (when should-send
                                                    (re-frame/dispatch [:buidl/next-step]))))
                      :on-change              (fn [e]
                                                (let [native-event (.-nativeEvent e)
                                                      text         (.-text native-event)]
                                                  (re-frame/dispatch [:buidl/set-issue-input-field field text])))}]])

(defn new-issue-title []
  [react/view {:style {:flex-direction :column
                       :margin 5
                       :border-radius 5
                       :background-color colors/white}}
   [buidl-text-input :title]])

(defn new-issue-content []
  [react/view {:style {:flex-direction :column
                       :margin 5
                       :border-radius 5
                       :background-color colors/white}}
   [buidl-text-input :content]])

(views/defview new-issue-tags []
  (views/letsubs [tags [:buidl.new-issue.ui/tags]
                  available-tags [:buidl.new-issue.ui/available-tags]]
    [react/view {:style {:flex-direction :column
                         :margin 5
                         :border-radius 5
                         :background-color colors/white}}
     [buidl-text-input :tag]
     [react/view {:style {:background-color colors/gray-lighter
                          :flex-direction :row
                          :flex-wrap :wrap}}
      (doall
       (for [tag available-tags]
         ^{:key tag} [tag-view tag {:on-press #(re-frame/dispatch [:buidl/add-tag tag])}]))]]))

(views/defview new-issue [step]
  (case step
    :title [new-issue-title]
    :content [new-issue-content]
    :tags [new-issue-tags]
    nil))

(views/defview new-issue-button [step]
  (let [action (case step
                 nil #(re-frame/dispatch [:buidl/new-issue])
                 :tags #(re-frame/dispatch [:buidl/create-issue])
                 #(re-frame/dispatch [:buidl/next-step]))
        label (case step
                nil "New issue"
                :tags "Create issue"
                "Next step")]
    [react/touchable-highlight {:style {:height           34
                                        :width            34
                                        :justify-content  :center
                                        :align-items      :center
                                        :align-self       :center}
                                :on-press action}
     [react/view {:style {:border-radius 4
                          :background-color colors/blue-dark
                          :width 120
                          :height 30
                          :justify-content :center
                          :align-items :center
                          :align-content :center}}
      [react/text {:style {:color colors/white
                           :font-size 14}}
       label]]]))

(views/defview new-issue-view []
  (views/letsubs [{:keys [step] :as issue} [:buidl.ui/new-issue]]
    [react/view {:style {:flex 1
                         :flex-direction :column
                         :justify-content :space-between
                         :background-color colors/gray-lighter}}
     [buidl.views/issue-view issue]
     [react/view {:style {:flex 1}}]
     [new-issue step]
     [new-issue-button step]]))
