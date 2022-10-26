(ns status-im.ui2.screens.chat.composer.input
  (:require [quo.react-native :as rn]
            [status-im.i18n.i18n :as i18n]
            [status-im.utils.handlers :refer [<sub >evt]]
            [quo.design-system.colors :as quo.colors]
            [status-im.utils.utils :as utils.utils]
            [status-im.utils.platform :as platform]
            [clojure.string :as string]
            [reagent.core :as reagent]
            [status-im.chat.constants :as chat.constants]
            [status-im.ui2.screens.chat.composer.style :as style]
            [re-frame.core :as re-frame]
            [status-im.chat.models.mentions :as mentions]
            [quo2.foundations.colors :as colors]
            [quo.react]))

(defonce input-texts (atom {}))
(defonce mentions-enabled (reagent/atom {}))
(defonce chat-input-key (reagent/atom 1))

(re-frame/reg-fx
 :chat.ui/clear-inputs
 (fn []
   (reset! input-texts {})
   (reset! mentions-enabled {})
   (reset! chat-input-key 1)))

(defn input-focus [text-input-ref]
  (some-> ^js (quo.react/current-ref text-input-ref) .focus))

(defn show-send [{:keys [actions-ref send-ref sticker-ref]}]
  (when actions-ref
    (quo.react/set-native-props actions-ref #js {:width 0 :left -88}))
  (quo.react/set-native-props send-ref #js {:width nil :right nil})
  (when sticker-ref
    (quo.react/set-native-props sticker-ref #js {:width 0 :right -100})))

(defn hide-send [{:keys [actions-ref send-ref sticker-ref]}]
  (when actions-ref
    (quo.react/set-native-props actions-ref #js {:width nil :left nil}))
  (quo.react/set-native-props send-ref #js {:width 0 :right -100})
  (when sticker-ref
    (quo.react/set-native-props sticker-ref #js {:width nil :right nil})))

(defn reset-input [refs chat-id]
  (some-> ^js (quo.react/current-ref (:text-input-ref refs)) .clear)
  (swap! mentions-enabled update :render not)
  (swap! input-texts dissoc chat-id))

(defn clear-input [chat-id refs]
  (hide-send refs)
  (if (get @mentions-enabled chat-id)
    (do
      (swap! mentions-enabled dissoc chat-id)
      ;;we need this timeout, because if we clear text input and first index was a mention object with blue color,
      ;;after clearing text will be typed with this blue color, so we render white text first and then clear it
      (js/setTimeout #(reset-input refs chat-id) 50))
    (reset-input refs chat-id)))

(defn on-text-change [val chat-id]
  (swap! input-texts assoc chat-id val)
  ;;we still store it in app-db for mentions, we don't have reactions in views
  (>evt [:chat.ui/set-chat-input-text val]))

(defn on-selection-change [timeout-id last-text-change mentionable-users args]
  (let [selection (.-selection ^js (.-nativeEvent ^js args))
        start (.-start selection)
        end (.-end selection)]
    ;; NOTE(rasom): on iOS we do not dispatch this event immediately
    ;; because it is needed only in case if selection is changed without
    ;; typing. Timeout might be canceled on `on-change`.
    (when platform/ios?
      (reset!
       timeout-id
       (utils.utils/set-timeout
        #(>evt [::mentions/on-selection-change
                {:start start
                 :end   end}
                mentionable-users])
        50)))
    ;; NOTE(rasom): on Android we dispatch event only in case if there
    ;; was no text changes during last 50ms. `on-selection-change` is
    ;; dispatched after `on-change`, that's why there is no another way
    ;; to know whether selection was changed without typing.
    (when (and platform/android?
               (or (not @last-text-change)
                   (< 50 (- (js/Date.now) @last-text-change))))
      (>evt [::mentions/on-selection-change
             {:start start
              :end   end}
             mentionable-users]))))

(defn on-change [last-text-change timeout-id mentionable-users refs chat-id sending-image args]
  (let [text (.-text ^js (.-nativeEvent ^js args))
        prev-text (get @input-texts chat-id)]
    (when (and (seq prev-text) (empty? text) (not sending-image))
      (hide-send refs))
    (when (and (empty? prev-text) (seq text))
      (show-send refs))

    (when (and (not (get @mentions-enabled chat-id)) (string/index-of text "@"))
      (swap! mentions-enabled assoc chat-id true))

    ;; NOTE(rasom): on iOS `on-selection-change` is canceled in case if it
    ;; happens during typing because it is not needed for mention
    ;; suggestions calculation
    (when (and platform/ios? @timeout-id)
      (utils.utils/clear-timeout @timeout-id))
    (when platform/android?
      (reset! last-text-change (js/Date.now)))

    (on-text-change text chat-id)
    ;; NOTE(rasom): on iOS `on-change` is dispatched after `on-text-input`,
    ;; that's why mention suggestions are calculated on `on-change`
    (when platform/ios?
      (>evt [::mentions/calculate-suggestions mentionable-users]))))

(defn on-text-input [mentionable-users chat-id args]
  (let [native-event (.-nativeEvent ^js args)
        text (.-text ^js native-event)
        previous-text (.-previousText ^js native-event)
        range (.-range ^js native-event)
        start (.-start ^js range)
        end (.-end ^js range)]
    (when (and (not (get @mentions-enabled chat-id)) (string/index-of text "@"))
      (swap! mentions-enabled assoc chat-id true))

    (>evt
     [::mentions/on-text-input
      {:new-text      text
       :previous-text previous-text
       :start         start
       :end           end}])
    ;; NOTE(rasom): on Android `on-text-input` is dispatched after
    ;; `on-change`, that's why mention suggestions are calculated
    ;; on `on-change`
    (when platform/android?
      (>evt [::mentions/calculate-suggestions mentionable-users]))))

(defn text-input [{:keys [set-active-panel refs chat-id sending-image on-content-size-change]}]
  (let [cooldown-enabled? (<sub [:chats/current-chat-cooldown-enabled?])
        mentionable-users (<sub [:chats/mentionable-users])
        timeout-id (atom nil)
        last-text-change (atom nil)
        mentions-enabled (get @mentions-enabled chat-id)]

    [rn/text-input
     {:style                    (style/text-input)
      :ref                      (:text-input-ref refs)
      :max-font-size-multiplier 1
      :accessibility-label      :chat-message-input
      :text-align-vertical      :center
      :multiline                true
      :editable                 (not cooldown-enabled?)
      :blur-on-submit           false
      :auto-focus               false
      :on-focus                 #(set-active-panel nil)
      :max-length               chat.constants/max-text-size
      :placeholder-text-color   (:text-02 @quo.colors/theme)
      :placeholder              (if cooldown-enabled?
                                  (i18n/label :cooldown/text-input-disabled)
                                  (i18n/label :t/type-a-message))
      :underline-color-android  :transparent
      :auto-capitalize          :sentences
      :auto-correct             false
      :spell-check              false
      :on-content-size-change   on-content-size-change
      :on-selection-change      (partial on-selection-change timeout-id last-text-change mentionable-users)
      :on-change                (partial on-change last-text-change timeout-id mentionable-users refs chat-id sending-image)
      :on-text-input            (partial on-text-input mentionable-users chat-id)}
     (if mentions-enabled
       (for [[idx [type text]] (map-indexed
                                (fn [idx item]
                                  [idx item])
                                (<sub [:chat/input-with-mentions]))]
         ^{:key (str idx "_" type "_" text)}
         [rn/text (when (= type :mention) {:style {:color colors/primary-50}})
          text])
       (get @input-texts chat-id))]))
