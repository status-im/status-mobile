(ns quo2.components.messages.system-message
  (:require [status-im.i18n.i18n :as i18n]
            [quo.react-native :as rn]
            [quo.theme :as theme]
            [quo2.components.buttons.button :as button]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.components.avatars.user-avatar :as user-avatar]
            [quo2.components.avatars.icon-avatar :as icon-avatar]))

(def themes
  {:light {:text       colors/black
           :icon       colors/primary-50
           :time       colors/neutral-50
           :background colors/neutral-5}
   :dark  {:text       colors/white
           :icon       colors/primary-50
           :time       colors/neutral-40
           :background colors/neutral-95}})

(defn get-color [key]
  (get-in themes [(theme/get-theme) key]))

(defn sm-timestamp [timestamp-str]
  [rn/view {:margin-left 6}
   [text/text {:size  :label
               :style {:color          (get-color :time)
                       :text-transform :none}}
    timestamp-str]])

(defn sm-icon [icon]
  [rn/view {:align-items  :center
            :margin-right 8}
   [icon-avatar/icon-avatar {:size    :medium
                             :icon    icon
                             :color   :primary
                             :opacity 5}]])

(defmulti sm-render :type)

(defmethod sm-render :deleted [_]
  [rn/view {:align-items     :center
            :justify-content :space-between
            :flex            1
            :flex-direction  :row}
   [rn/view {:align-items    :center
             :flex-direction :row}
    [sm-icon :main-icons/placeholder16]
    [text/text {:size  :paragraph-2
                :style {:color        (get-color :text)
                        :margin-right 5}}
     (i18n/label :message-deleted-for-everyone)]]
   [button/button {:size   24
                   :before :main-icons/timeout
                   :type   :grey} (i18n/label :undo)]])

(defmethod sm-render :added [{:keys [mentions timestamp-str]}]
  [rn/view {:align-items    :center
            :flex-direction :row}
   [sm-icon :main-icons/placeholder16]
   [rn/view {:margin-right 4}
    [user-avatar/user-avatar {:status-indicator? false
                              :online?           false
                              :size              :xxxs
                              :profile-picture   (:image (first mentions))
                              :ring?             false}]]
   [text/text {:weight :semi-bold
               :size   :paragraph-2}
    (:name (first mentions))]
   [text/text {:size  :paragraph-2
               :style {:color        (get-color :text)
                       :margin-left  3
                       :margin-right 3}}
    (i18n/label :added)]
   [rn/view {:margin-right 4}
    [user-avatar/user-avatar {:status-indicator? false
                              :online?           false
                              :size              :xxxs
                              :profile-picture   (:image (second mentions))
                              :ring?             false}]]
   [text/text {:weight :semi-bold
               :size   :paragraph-2}
    (:name (second mentions))]
   [sm-timestamp timestamp-str]])

(defmethod sm-render :pinned [{:keys [pinned-by content timestamp-str]}]
  [rn/view {:flex-direction :row
            :flex           1
            :align-items    :center}
   [sm-icon :main-icons/pin16]
   [rn/view {:flex-direction :column
             :flex           1}
    [rn/view {:align-items    :baseline
              :flex-direction :row}
     [text/text {:size   :paragraph-2
                 :weight :semi-bold
                 :style  {:color (get-color :text)}}
      pinned-by]
     [rn/view {:margin-left  4
               :margin-right 2}
      [text/text {:size  :paragraph-2
                  :style {:color (get-color :text)}}
       (i18n/label :pinned-a-message)]]
     [sm-timestamp timestamp-str]]
    [rn/view {:flex-direction :row}
     [rn/view {:flex-direction :row
               :margin-right   4}
      [rn/view {:margin-right 4}
       [user-avatar/user-avatar {:status-indicator? false
                                 :online?           false
                                 :size              :xxxs
                                 :profile-picture   (:image (:mentions content))
                                 :ring?             false}]]
      [text/text {:weight :semi-bold
                  :size   :label}
       (:name (:mentions content))]]
     (when (seq (:text content))
       [rn/view {:margin-right   20
                 :flex-direction :row
                 :flex           1}
        [text/text {:size            :label
                    :style           {:color (get-color :text)}
                    :number-of-lines 1
                    :ellipsize-mode  :tail}
         (:text content)]])
     [rn/view {:justify-content :flex-end
               :flex-direction  :row
               :min-width       10}
      (when (seq (:info content))
        [text/text {:size  :label
                    :style {:color (get-color :time)}}
         (:info content)])]]]])

(defn system-message
  [{:keys [unread?] :as message}]
  [rn/view {:flex-direction     :row
            :flex               1
            :border-radius      16
            :padding-vertical   9
            :padding-horizontal 11
            :width              359
            :height             52
            :background-color   (when unread? (get-color :background))}
   [sm-render message]])
