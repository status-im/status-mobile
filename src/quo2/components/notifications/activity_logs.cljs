(ns quo2.components.notifications.activity-logs
  (:require [quo.core :as quo]
            [quo2.components.buttons.button :as button]
            [quo2.components.icon :as icon]
            [quo2.components.markdown.text :as text]
            [quo2.components.tags.status-tags :as status-tags]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]))

(def ^:private max-reply-length
  280)

(defn- valid-reply?
  [reply]
  (<= (count reply) max-reply-length))

(defn- activity-reply-text-input
  [reply-input on-update-reply]
  [rn/view
   [rn/view {:style {:margin-top     16
                     :margin-bottom  8
                     :flex-direction :row}}
    [text/text {:weight :medium
                :style  {:flex-grow 1
                         :color     colors/neutral-40}}
     (i18n/label :t/your-answer)]
    [text/text {:style {:flex-shrink 1
                        :color       (if (valid-reply? @reply-input)
                                       colors/neutral-40
                                       colors/danger-60)}}
     (str (count @reply-input) "/" max-reply-length)]]
   [rn/view
    ;; TODO(@ilmotta): Replace with quo2 component when available.
    ;; https://github.com/status-im/status-mobile/issues/14364
    [quo/text-input
     {:on-change-text      #(do (reset! reply-input %)
                                (when on-update-reply
                                  (on-update-reply %)))
      :auto-capitalize     :none
      :auto-focus          true
      :accessibility-label :identity-verification-reply-text-input
      :placeholder         (i18n/label :t/type-something)
      :return-key-type     :none
      :multiline           false
      :auto-correct        false}]]])

(defn- activity-icon
  [icon]
  [rn/view {:height          32
            :width           32
            :border-radius   100
            :margin-top      10
            :border-width    1
            :border-color    colors/white-opa-5
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
  [context replying?]
  (let [first-line-offset (if replying? 4 -2)
        gap-between-lines 4]
    (into [rn/view {:flex-direction :row
                    :align-items    :center
                    :flex-wrap      :wrap
                    :margin-top     first-line-offset}]
          (map-indexed (fn [index detail]
                         ^{:key index}
                         [rn/view {:margin-right 4
                                   :margin-top   gap-between-lines}
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
            :background-color   colors/white-opa-5}
   (when title
     [text/text {:size  :paragraph-2
                 :style {:color         colors/white-opa-40
                         :margin-bottom 2}}
      title])
   (if (string? body)
     [text/text {:style {:color colors/white}
                 :size  :paragraph-1}
      body]
     body)])

(defn- activity-buttons
  [button-1 button-2 replying? reply-input]
  (let [size         (if replying? 40 24)
        common-style (when replying?
                       {:padding-vertical 9
                        :flex-grow        1
                        :flex-basis       0})]
    [rn/view {:margin-top     12
              :flex-direction :row
              :align-items    :flex-start}
     (when button-1
       [button/button (-> button-1
                          (assoc :size size)
                          (update :style merge common-style {:margin-right 8}))
        (:label button-1)])
     (when button-2
       [button/button (-> button-2
                          (assoc :size size)
                          (assoc :disabled (and replying? (not (valid-reply? @reply-input))))
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
  [title replying?]
  [text/text {:weight :semi-bold
              :style  {:color colors/white}
              :size   (if replying? :heading-2 :paragraph-1)}
   title])

(defn- activity-timestamp
  [timestamp]
  [rn/view {:margin-left 8}
   [text/text {:size  :label
               :style {:text-transform :none
                       :color          colors/neutral-40}}
    timestamp]])

(defn- footer
  [_]
  (let [reply-input (reagent/atom "")]
    (fn [{:keys [replying? on-update-reply status button-1 button-2]}]
      [:<>
       (when replying?
         [activity-reply-text-input reply-input on-update-reply])
       (cond (some? status)
             [activity-status status]

             (or button-1 button-2)
             [activity-buttons button-1 button-2 replying? reply-input])])))

(defn activity-log
  [{:keys [icon
           message
           context
           timestamp
           title
           replying?
           unread?]
    :as   props}]
  [rn/view {:flex-direction     :row
            :align-items        :flex-start
            :border-radius      16
            :padding-top        8
            :padding-horizontal (if replying? 20 12)
            :padding-bottom     12
            :background-color   (when (and unread? (not replying?))
                                  colors/primary-50-opa-10)}
   (when-not replying?
     [activity-icon icon])
   [rn/view {:padding-left (when-not replying? 8)
             :flex-grow    1}
    [rn/view {:flex-grow      1
              :align-items    :center
              :flex-direction :row}
     [rn/view {:flex           1
               :align-items    :center
               :flex-direction :row}
      [rn/view {:flex-shrink 1}
       [activity-title title replying?]]
      (when-not replying?
        [activity-timestamp timestamp])]
     (when (and unread? (not replying?))
       [activity-unread-dot])]
    (when context
      [activity-context context replying?])
    (when message
      [activity-message message])
    [footer props]]])
