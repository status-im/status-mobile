(ns quo2.components.notifications.activity-logs
  (:require [react-native.core :as rn]
            [quo2.components.buttons.button :as button]
            [quo2.components.icon :as icon]
            [quo2.components.markdown.text :as text]
            [quo2.components.tags.status-tags :as status-tags]
            [quo2.foundations.colors :as colors]))

(defn- activity-icon
  [icon]
  [rn/view {:height          32
            :width           32
            :border-radius   100
            :margin-top      10
            :border-width    1
            :border-color    colors/white-opa-5
            :flex-direction  :column
            :align-items     :center
            :justify-content :center}
   [icon/icon icon {:color colors/white}]])

(defn- activity-unread-dot
  []
  [rn/view {:margin-left      14
            :margin-right     6
            :background-color colors/primary-50
            :width            8
            :height           8
            :border-radius    4}])

(defn- activity-context
  [context]
  (let [margin-top 4]
    (into [rn/view {:flex           1
                    :flex-direction :row
                    :align-items    :center
                    :flex-wrap      :wrap
                    :margin-top     (+ 4 (- margin-top))}]
          (map-indexed (fn [index detail]
                         ^{:key index}
                         [rn/view {:margin-right 4
                                   :margin-top   margin-top}
                          (if (string? detail)
                            [text/text {:size :paragraph-2}
                             detail]
                            detail)])
                       context))))

(defn- activity-message
  [{:keys [title body]}]
  [rn/view {:border-radius      12
            :margin-top         12
            :padding-horizontal 12
            :padding-vertical   8
            :background-color   colors/white-opa-5
            :flex               1
            :flex-direction     :column}
   (when title
     [text/text {:size  :paragraph-2
                 :style {:color colors/white-opa-40}}
      title])
   (if (string? body)
     [text/text {:style {:color colors/white}
                 :size  :paragraph-1}
      body]
     body)])

(defn- activity-buttons
  [button-1 button-2]
  (let [size         24
        common-style {:padding-top    3
                      :padding-right  8
                      :padding-bottom 4
                      :padding-left   8}]
    [rn/view {:margin-top     12
              :flex           1
              :flex-direction :row
              :align-items    :flex-start}
     (when button-1
       [button/button (-> button-1
                          (assoc :size size)
                          (assoc-in [:style :margin-right] 8)
                          (update :style merge common-style))
        (:label button-1)])
     (when button-2
       [button/button (-> button-2
                          (assoc :size size)
                          (update :style merge common-style))
        (:label button-2)])]))

(defn- activity-status
  [status]
  [rn/view {:margin-top  12
            :align-items :flex-start
            :flex        1}
   [status-tags/status-tag {:size   :small
                            :label  (:label status)
                            :status status}]])

(defn- activity-title
  [title]
  [text/text {:weight :semi-bold
              :style  {:color colors/white}
              :size   :paragraph-1}
   title])

(defn- activity-timestamp
  [timestamp]
  [rn/view {:margin-left 8}
   [text/text {:size  :label
               :style {:text-transform :none
                       :color          colors/neutral-40}}
    timestamp]])

(defn activity-log
  [{:keys [button-1
           button-2
           icon
           message
           status
           context
           timestamp
           title
           unread?]}]
  [rn/view {:flex-direction     :row
            :flex               1
            :border-radius      16
            :padding-top        8
            :padding-horizontal 12
            :padding-bottom     12
            :background-color   (when unread?
                                  colors/primary-50-opa-10)}
   [activity-icon icon]
   [rn/view {:flex-direction :column
             :padding-left   8
             :flex           1}
    [rn/view {:flex           1
              :align-items    :center
              :flex-direction :row}
     [rn/view {:flex           1
               :align-items    :center
               :flex-direction :row}
      [rn/view {:flex-shrink 1}
       [activity-title title]]
      [activity-timestamp timestamp]]
     (when unread?
       [activity-unread-dot])]
    (when context
      [activity-context context])
    (when message
      [activity-message message])
    (cond
      (some? status)
      [activity-status status]

      (or button-1 button-2)
      [activity-buttons button-1 button-2])]])
