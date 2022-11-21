(ns quo2.components.notifications.activity-log.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [quo2.components.buttons.button :as button]
            [quo2.components.icon :as icon]
            [quo2.components.markdown.text :as text]
            [quo2.components.tags.status-tags :as status-tags]
            [quo2.foundations.colors :as colors]
            [quo2.components.notifications.activity-log.style :as style]
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
  [rn/view {:style style/icon}
   [icon/icon icon {:color colors/white}]])

(defn- activity-context
  [context replying?]
  (let [first-line-offset (if replying? 4 0)
        gap-between-lines 4]
    (into [rn/view {:style (assoc style/context-container :margin-top first-line-offset)}]
          (mapcat (fn [detail]
                    ^{:key (hash detail)}
                    (if (string? detail)
                      (map (fn [s]
                             [rn/view {:style {:margin-right 4
                                               :margin-top   0}}
                              [text/text {:size :paragraph-2}
                               s]])
                           (string/split detail #"\s+"))
                      [[rn/view {:margin-right 4
                                 :margin-top   gap-between-lines}
                        detail]]))
                  context))))

(defn- activity-message
  [{:keys [title body]}]
  [rn/view {:style style/message-container}
   (when title
     [text/text {:size                :paragraph-2
                 :accessibility-label :activity-message-title
                 :style               style/message-title}
      title])
   (if (string? body)
     [text/text {:style               style/message-body
                 :accessibility-label :activity-message-body
                 :size                :paragraph-1}
      body]
     body)])

(defn- activity-buttons
  [button-1 button-2 replying? reply-input]
  (let [size         (if replying? 40 24)
        common-style (when replying?
                       {:padding-vertical 9
                        :flex-grow        1
                        :flex-basis       0})]
    [rn/view style/buttons-container
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
  [rn/view {:style               style/status
            :accessibility-label :activity-status}
   [status-tags/status-tag {:size   :small
                            :label  (:label status)
                            :status status}]])

(defn- activity-title
  [title replying?]
  [text/text {:weight              :semi-bold
              :accessibility-label :activity-title
              :style               (style/title replying?)
              :size                (if replying? :heading-2 :paragraph-1)}
   title])

(defn- activity-timestamp
  [timestamp]
  [text/text {:size                :label
              :accessibility-label :activity-timestamp
              :style               style/timestamp}
   timestamp])

(defn- activity-unread-dot
  []
  [rn/view {:accessibility-label :activity-unread-indicator
            :style               style/unread-dot-container}
   [rn/view {:style style/unread-dot}]])

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

(defn view
  [{:keys [icon
           message
           context
           timestamp
           title
           replying?
           unread?]
    :as   props}]
  [rn/view {:accessibility-label :activity
            :style               style/container}
   (when-not replying?
     [activity-icon icon])
   [rn/view {:style {:padding-left (when-not replying? 8)
                     :flex         1}}
    [rn/view
     [rn/view {:style style/top-section-container}
      [activity-title title replying?]
      (when-not replying?
        [activity-timestamp timestamp])
      (when (and unread? (not replying?))
        [activity-unread-dot])]
     (when context
       [activity-context context replying?])]
    (when message
      [activity-message message])
    [footer props]]])
