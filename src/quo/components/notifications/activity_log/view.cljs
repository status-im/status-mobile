(ns quo.components.notifications.activity-log.view
  (:require
    [clojure.string :as string]
    [quo.components.buttons.button.view :as button]
    [quo.components.icon :as icon]
    [quo.components.inputs.input.view :as input]
    [quo.components.markdown.text :as text]
    [quo.components.notifications.activity-log.style :as style]
    [quo.components.tags.status-tags :as status-tags]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [utils.i18n :as i18n]))

(defn- activity-reply-text-input
  [{:keys [on-update-reply max-reply-length valid-reply?]} reply-input set-reply-input]
  [rn/view
   [rn/view
    {:style {:margin-top     16
             :margin-bottom  8
             :flex-direction :row}}
    [text/text
     {:weight :medium
      :style  {:flex-grow 1
               :color     colors/neutral-40}}
     (i18n/label :t/your-answer)]
    [text/text
     {:style {:flex-shrink 1
              :color       (if (valid-reply? reply-input)
                             colors/neutral-40
                             colors/danger-60)}}
     (str (count reply-input) "/" max-reply-length)]]
   [input/input
    {:on-change-text      #(do (set-reply-input %)
                               (when on-update-reply
                                 (on-update-reply %)))
     :auto-capitalize     :none
     :auto-focus          false
     :accessibility-label :identity-verification-reply-text-input
     :placeholder         (i18n/label :t/type-something)
     :return-key-type     :none
     :multiline           false
     :auto-correct        false}]])

(defn- activity-icon
  [icon]
  [rn/view {:style style/icon}
   [icon/icon icon {:color colors/white}]])

(defn- activity-context
  [context replying?]
  (let [first-line-offset (if replying? 4 0)
        gap-between-lines 5]
    (into [rn/view {:style (assoc style/context-container :margin-top first-line-offset)}]
          (mapcat
           (fn [detail]
             ^{:key (hash detail)}
             (if (string? detail)
               (map (fn [s]
                      [rn/view
                       {:style {:margin-right 4
                                :margin-top   0}}
                       [text/text
                        {:size  :paragraph-2
                         :style {:color colors/white}}
                        s]])
                    (string/split detail #"\s+"))
               [[rn/view
                 {:margin-right 4
                  :margin-top   gap-between-lines}
                 detail]]))
           context))))

(defn- hiccup-props
  [body]
  (and (vector? body)
       (map? (second body))
       (second body)))

(defn- activity-message
  [{:keys [title body title-number-of-lines body-number-of-lines attachment]}]
  (let [{:keys [photos message-text]} (hiccup-props body)
        text-with-photos?             (and (not (string/blank? message-text))
                                           (seq photos))]
    [rn/view {:style (style/message-container attachment text-with-photos?)}
     (when title
       [text/text
        {:size                :paragraph-2
         :accessibility-label :activity-message-title
         :style               style/message-title
         :number-of-lines     title-number-of-lines}
        title])
     (if (string? body)
       [text/text
        {:style               style/message-body
         :accessibility-label :activity-message-body
         :size                :paragraph-1
         :number-of-lines     body-number-of-lines}
        body]
       body)]))

(defn- activity-title
  [title replying?]
  [text/text
   {:weight              :semi-bold
    :accessibility-label :activity-title
    :style               (style/title)
    :size                (if replying? :heading-2 :paragraph-1)}
   title])

(defn- activity-timestamp
  [timestamp]
  [text/text
   {:size                :label
    :accessibility-label :activity-timestamp
    :style               style/timestamp}
   timestamp])

(defn- activity-unread-dot
  [customization-color]
  [rn/view
   {:accessibility-label :activity-unread-indicator
    :style               style/unread-dot-container}
   [rn/view {:style (style/unread-dot customization-color)}]])

(defmulti footer-item-view (fn [item _ _] (:type item)))

(defmethod footer-item-view :button
  [{:keys [label subtype disable-when] :as button} replying? reply-input]
  (let [size         (if replying? 40 24)
        common-style (when replying?
                       {:padding-vertical 9
                        :flex-grow        1
                        :flex-basis       0})]
    [button/button
     (-> button
         (assoc :size size)
         (assoc :type subtype)
         (assoc :disabled? (and replying? disable-when (disable-when reply-input)))
         (assoc :inner-style
                {:justify-content :center
                 :padding-bottom  0
                 :padding-top     0})
         (update :container-style merge common-style {:margin-right 8}))
     label]))

(defmethod footer-item-view :status
  [{:keys [label subtype blur? theme]} _ _]
  [quo.theme/provider {:theme theme}
   [status-tags/status-tag
    {:size   :small
     :label  label
     :status {:type subtype}
     :blur?  blur?}]])

(defn- footer
  [{:keys [replying? items] :as props}]
  (let [[reply-input set-reply-input] (rn/use-state "")]
    [:<>
     (when replying?
       [activity-reply-text-input props reply-input set-reply-input])
     (when items
       [rn/view style/footer-container
        (for [item items]
          ^{:key (:key item)}
          [footer-item-view item replying? reply-input])])]))

(defn view
  [{:keys [icon
           message
           context
           timestamp
           title
           replying?
           unread?
           customization-color]
    :as   props}]
  [rn/view
   {:accessibility-label :activity
    :style               style/container
    :on-layout           (:on-layout props)}
   (when-not replying?
     [activity-icon icon])
   [rn/view
    {:style {:padding-left (when-not replying? 8)
             :flex         1}}
    [rn/view
     [rn/view {:style style/top-section-container}
      [rn/view {:style style/title-container}
       [activity-title title replying?]
       (when-not replying?
         [activity-timestamp timestamp])]
      (when (and unread? (not replying?))
        [activity-unread-dot customization-color])]
     (when context
       [activity-context context replying?])]
    (when message
      [activity-message message])
    [footer props]]])
