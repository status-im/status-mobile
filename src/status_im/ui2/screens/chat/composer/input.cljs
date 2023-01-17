(ns status-im.ui2.screens.chat.composer.input
  (:require ["react-native" :as react-native]
            [clojure.string :as string]
            [oops.core :as oops]
            [quo.design-system.colors :as quo.colors]
            [quo.react]
            [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.chat.constants :as chat.constants]
            [status-im.chat.models.mentions :as mentions]
            [i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [utils.re-frame :as rf]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils.utils]
            [utils.transforms :as transforms]
            [quo2.foundations.typography :as typography]))

(defonce input-texts (atom {}))
(defonce input-text-content-heights (atom {}))
(defonce mentions-enabled? (reagent/atom {}))
(defonce chat-input-key (reagent/atom 1))
(defonce text-input-ref (reagent/atom nil))

(declare selectable-text-input)

(re-frame/reg-fx
 :chat.ui/clear-inputs
 (fn []
   (reset! input-texts {})
   (reset! input-text-content-heights {})
   (reset! mentions-enabled? {})
   (reset! chat-input-key 1)))

(defn input-focus
  [text-input-ref]
  (some-> ^js (quo.react/current-ref text-input-ref)
          .focus))

(defn show-send
  [{:keys [actions-ref send-ref sticker-ref]}]
  (when actions-ref
    (quo.react/set-native-props actions-ref #js {:width 0 :left -88}))
  (quo.react/set-native-props send-ref #js {:width nil :right nil})
  (when sticker-ref
    (quo.react/set-native-props sticker-ref #js {:width 0 :right -100})))

(defn hide-send
  [{:keys [actions-ref send-ref sticker-ref]}]
  (when actions-ref
    (quo.react/set-native-props actions-ref #js {:width nil :left nil}))
  (quo.react/set-native-props send-ref #js {:width 0 :right -100})
  (when sticker-ref
    (quo.react/set-native-props sticker-ref #js {:width nil :right nil})))

(defn reset-input
  [refs chat-id]
  (some-> ^js (quo.react/current-ref (:text-input-ref refs))
          .clear)
  (swap! mentions-enabled? update :render not)
  (swap! input-texts dissoc chat-id)
  (swap! input-text-content-heights dissoc chat-id))

(defn clear-input
  [chat-id refs]
  (hide-send refs)
  (if (get @mentions-enabled? chat-id)
    (do
      (swap! mentions-enabled? dissoc chat-id)
      ;;we need this timeout, because if we clear text input and first index was a mention object with
      ;;blue color,
      ;;after clearing text will be typed with this blue color, so we render white text first and then
      ;;clear it
      (js/setTimeout #(reset-input refs chat-id) 50))
    (reset-input refs chat-id)))

(defn on-text-change
  [val chat-id]
  (println "on=text-change" val)
  (swap! input-texts assoc chat-id val)
  ;;we still store it in app-db for mentions, we don't have reactions in views
  (rf/dispatch [:chat.ui/set-chat-input-text val]))

(defn on-selection-change
  [timeout-id last-text-change mentionable-users args]
  (let [selection (.-selection ^js (.-nativeEvent ^js args))
        start     (.-start selection)
        end       (.-end selection)]
    ;; NOTE(rasom): on iOS we do not dispatch this event immediately
    ;; because it is needed only in case if selection is changed without
    ;; typing. Timeout might be canceled on `on-change`.
    (when platform/ios?
      (reset!
        timeout-id
        (utils.utils/set-timeout
         #(rf/dispatch [::mentions/on-selection-change
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
      (rf/dispatch [::mentions/on-selection-change
                    {:start start
                     :end   end}
                    mentionable-users]))))

(defn on-change
  [last-text-change timeout-id mentionable-users refs chat-id sending-image args]
  (let [text      (.-text ^js (.-nativeEvent ^js args))
        prev-text (get @input-texts chat-id)]
    (when (and (seq prev-text) (empty? text) (not sending-image))
      (hide-send refs))
    (when (and (empty? prev-text) (or (seq text) sending-image))
      (show-send refs))

    (when (and (not (get @mentions-enabled? chat-id)) (string/index-of text "@"))
      (swap! mentions-enabled? assoc chat-id true))

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
      (rf/dispatch [::mentions/calculate-suggestions mentionable-users]))))

(defn on-text-input
  [mentionable-users chat-id args]
  (let [native-event  (.-nativeEvent ^js args)
        text          (.-text ^js native-event)
        previous-text (.-previousText ^js native-event)
        range         (.-range ^js native-event)
        start         (.-start ^js range)
        end           (.-end ^js range)]
    (when (and (not (get @mentions-enabled? chat-id)) (string/index-of text "@"))
      (swap! mentions-enabled? assoc chat-id true))

    (rf/dispatch
     [::mentions/on-text-input
      {:new-text      text
       :previous-text previous-text
       :start         start
       :end           end}])
    ;; NOTE(rasom): on Android `on-text-input` is dispatched after
    ;; `on-change`, that's why mention suggestions are calculated
    ;; on `on-change`
    (when platform/android?
      (rf/dispatch [::mentions/calculate-suggestions mentionable-users]))))

(defn text-input-style
  []
  (merge typography/font-regular
         typography/paragraph-1
         {:flex              1
          :min-height        34
          :margin            0
          :flex-shrink       1
          :color             (:text-01 @quo.colors/theme)
          :margin-horizontal 20}
         (if platform/android?
           {:padding-vertical    8
            :text-align-vertical :top}
           {:margin-top    8
            :margin-bottom 8})))

(defn text-input
  [{:keys [refs chat-id sending-image on-content-size-change]}]
  (let [cooldown-enabled?   (rf/sub [:chats/current-chat-cooldown-enabled?])
        mentionable-users   (rf/sub [:chats/mentionable-users])
        timeout-id          (reagent/atom nil)
        last-text-change    (reagent/atom nil)
        mentions-enabled?   (get @mentions-enabled? chat-id)
        props
        {:style                    (text-input-style)
         :ref                      (:text-input-ref refs)
         :max-font-size-multiplier 1
         :accessibility-label      :chat-message-input
         :text-align-vertical      :center
         :multiline                true
         :editable                 (not cooldown-enabled?)
         :blur-on-submit           false
         :auto-focus               false
         :max-length               chat.constants/max-text-size
         :placeholder-text-color   (:text-02 @quo.colors/theme)
         :placeholder              (if cooldown-enabled?
                                     (i18n/label :cooldown/text-input-disabled)
                                     (i18n/label :t/type-a-message))
         :default-value            (get @input-texts chat-id)
         :underline-color-android  :transparent
         :auto-capitalize          :sentences
         :auto-correct             false
         :spell-check              false
         :on-content-size-change   on-content-size-change
         :on-selection-change      (partial on-selection-change
                                            timeout-id
                                            last-text-change
                                            mentionable-users)
         :on-change
         (partial on-change last-text-change timeout-id mentionable-users refs chat-id sending-image)
         :on-text-input            (partial on-text-input mentionable-users chat-id)}
        input-with-mentions (rf/sub [:chat/input-with-mentions])
        children            (fn []
                              (if mentions-enabled?
                                (map-indexed
                                 (fn [index [_ text]]
                                   ^{:key (str index "_" type "_" text)}
                                   [rn/text (when (= type :mention) {:style {:color colors/primary-50}})
                                    text])
                                 input-with-mentions)
                                (get @input-texts chat-id)))]
    (reset! text-input-ref (:text-input-ref refs))
    ;when ios implementation for selectable-text-input is ready, we need remove this condition and use
    ;selectable-text-input directly.
    (if platform/android?
      [selectable-text-input chat-id props children]
      [rn/text-input props
       children])))

(defn selectable-text-input-manager
  []
  (when (exists? (.-NativeModules react-native))
    (.-RNSelectableTextInputManager ^js (.-NativeModules react-native))))

(defonce rn-selectable-text-input
         (reagent/adapt-react-class (.requireNativeComponent react-native "RNSelectableTextInput")))

(declare first-level-menu-items second-level-menu-items)

(defn update-input-text
  [{:keys [text-input chat-id]} text]
  (on-text-change text chat-id)
  (.setNativeProps ^js text-input (clj->js {:text text})))

(re-frame/reg-fx
 :set-text-input-value
 (fn [[chat-id text local-text-input-ref]]
   (when local-text-input-ref
     (reset! text-input-ref local-text-input-ref))
   (on-text-change text chat-id)
   (if platform/ios?
     (.setNativeProps ^js (quo.react/current-ref @text-input-ref) (clj->js {:text text}))
     (if (string/blank? text)
       (.clear ^js (quo.react/current-ref @text-input-ref))
       (.setNativeProps ^js (quo.react/current-ref @text-input-ref) (clj->js {:text text}))))))

(defn calculate-input-text
  [{:keys [full-text selection-start selection-end]} content]
  (let [head (subs full-text 0 selection-start)
        tail (subs full-text selection-end)]
    (str head content tail)))

(defn update-selection
  [text-input-handle selection-start selection-end]
  ;to avoid something disgusting like this
  ;https://lightrun.com/answers/facebook-react-native-textinput-controlled-selection-broken-on-both-ios-and-android
  ;use native invoke instead! do not use setNativeProps! e.g. (.setNativeProps ^js text-input (clj->js
  ;{:selection {:start selection-start :end selection-end}}))
  (let [manager (selectable-text-input-manager)]
    (oops/ocall manager :setSelection text-input-handle selection-start selection-end)))

(def first-level-menus
  {:cut               (fn [{:keys [content] :as params}]
                        (let [new-text (calculate-input-text params "")]
                          (react/copy-to-clipboard content)
                          (update-input-text params new-text)))

   :copy-to-clipboard (fn [{:keys [content]}]
                        (react/copy-to-clipboard content))

   :paste             (fn [params]
                        (let [callback (fn [paste-content]
                                         (let [content  (string/trim paste-content)
                                               new-text (calculate-input-text params content)]
                                           (update-input-text params new-text)))]
                          (react/get-from-clipboard callback)))

   :biu               (fn [{:keys [first-level text-input-handle menu-items selection-start
                                   selection-end]}]
                        (reset! first-level false)
                        (reset! menu-items second-level-menu-items)
                        (update-selection text-input-handle selection-start selection-end))})

(def first-level-menu-items (map i18n/label (keys first-level-menus)))

(defn reset-to-first-level-menu
  [first-level menu-items]
  (reset! first-level true)
  (reset! menu-items first-level-menu-items))

(defn append-markdown-char
  [{:keys [first-level menu-items content selection-start selection-end text-input-handle
           selection-event]
    :as   params} wrap-chars]
  (let [content         (str wrap-chars content wrap-chars)
        new-text        (calculate-input-text params content)
        len-wrap-chars  (count wrap-chars)
        selection-start (+ selection-start len-wrap-chars)
        selection-end   (+ selection-end len-wrap-chars)]
    ;don't update selection directly here, process it within on-selection-change instead
    ;so that we can avoid java.lang.IndexOutOfBoundsException: setSpan..
    (reset! selection-event {:start             selection-start
                             :end               selection-end
                             :text-input-handle text-input-handle})
    (update-input-text params new-text)
    (reset-to-first-level-menu first-level menu-items)))

(def second-level-menus
  {:bold          #(append-markdown-char % "**")

   :italic        #(append-markdown-char % "*")

   :strikethrough #(append-markdown-char % "~~")})

(def second-level-menu-items (map i18n/label (keys second-level-menus)))

(defn on-menu-item-touched
  [{:keys [first-level event-type] :as params}]
  (let [menus         (if @first-level first-level-menus second-level-menus)
        menu-item-key (nth (keys menus) event-type)
        action        (get menus menu-item-key)]
    (action params)))

(defn selectable-text-input
  [_ _ _]
  (let [text-input-ref  (reagent/atom nil)
        menu-items      (reagent/atom first-level-menu-items)
        first-level     (reagent/atom true)
        selection-event (atom nil)
        manager         (selectable-text-input-manager)]
    (reagent/create-class
     {:component-did-mount
      (fn [this]
        (when @text-input-ref
          (let [selectable-text-input-handle (rn/find-node-handle this)
                text-input-handle            (rn/find-node-handle @text-input-ref)]
            (oops/ocall manager :setupMenuItems selectable-text-input-handle text-input-handle))))

      :component-did-update (fn [_ _ _ _]
                              (when (not @first-level)
                                (let [text-input-handle (rn/find-node-handle @text-input-ref)]
                                  (oops/ocall manager :startActionMode text-input-handle))))

      :reagent-render
      (fn [chat-id {:keys [style ref on-selection-change] :as props} children]
        (let [ref                 #(do (reset! text-input-ref %)
                                       (when ref
                                         (quo.react/set-ref-val! ref %)))
              on-selection-change (fn [args]
                                    (let [selection    (.-selection ^js (.-nativeEvent ^js args))
                                          start        (.-start selection)
                                          end          (.-end selection)
                                          no-selection (<= (- end start) 0)]
                                      (when (and no-selection (not @first-level))
                                        (oops/ocall manager :hideLastActionMode)
                                        (reset-to-first-level-menu first-level menu-items)))
                                    (when on-selection-change
                                      (on-selection-change args))
                                    (when @selection-event
                                      (let [{:keys [start end text-input-handle]} @selection-event]
                                        (update-selection text-input-handle start end)
                                        (reset! selection-event nil))))
              on-selection
              (fn [^js event]
                (let [native-event (.-nativeEvent event)
                      native-event (transforms/js->clj native-event)
                      {:keys [eventType content selectionStart selectionEnd]} native-event
                      full-text (:input-text (rf/sub [:chats/current-chat-input]))]
                  (on-menu-item-touched {:first-level       first-level
                                         :event-type        eventType
                                         :content           content
                                         :selection-start   selectionStart
                                         :selection-end     selectionEnd
                                         :text-input        @text-input-ref
                                         :text-input-handle (rn/find-node-handle @text-input-ref)
                                         :full-text         full-text
                                         :menu-items        menu-items
                                         :chat-id           chat-id
                                         :selection-event   selection-event})))
              props               (merge props
                                         {:ref                 ref
                                          :style               (dissoc style :margin-horizontal)
                                          :on-selection-change on-selection-change
                                          :on-selection        on-selection})]
          [rn-selectable-text-input {:menuItems @menu-items :style style}
           [rn/text-input props
            children]]))})))
