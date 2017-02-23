(ns status-im.chat.views.message-input
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                text
                                                animated-view
                                                icon
                                                touchable-highlight
                                                text-input]]
            [status-im.chat.views.plain-message :as plain-message]
            [status-im.chat.views.command :as command]
            [status-im.chat.styles.message-input :as st]
            [status-im.chat.styles.plain-message :as st-message]
            [status-im.chat.styles.response :as st-response]
            [status-im.accessibility-ids :as id]
            [reagent.core :as r]
            [clojure.string :as str]
            [taoensso.timbre :as log]
            [clojure.string :as s]
            [status-im.commands.utils :refer [command-prefix]]))

(defn send-button [{:keys [on-press accessibility-label]}]
  [touchable-highlight {:on-press            on-press
                        :accessibility-label accessibility-label}
   [view st/send-wrapper
    [view st/send-container
     [icon :send st/send-icon]]]])

(defn plain-input-options [{:keys [set-layout-size-fn disable?]}]
  {:style                  st-message/message-input
   :on-change-text         (when-not disable? plain-message/set-input-message)
   :on-submit-editing      plain-message/send
   :on-content-size-change #(let [size (-> (.-nativeEvent %)
                                           (.-contentSize)
                                           (.-height))]
                              (set-layout-size-fn size))
   :on-focus               #(do (dispatch [:set :focused true])
                                (dispatch [:set-chat-ui-props :show-emoji? false]))
   :on-blur                #(do (dispatch [:set :focused false])
                                (set-layout-size-fn 0))
   :blur-on-submit         true
   :multiline              true
   :editable               (not disable?)})

(defn command-input-options [{:keys [icon-width disable? sending-disabled? flag]}]
  {:style             (st-response/command-input icon-width disable?)
   :on-change-text    (when-not disable? (fn [text]
                                           ;; update flag only when the first
                                           ;; character was added to commands input
                                           (when (and (s/starts-with? text command-prefix)
                                                      (= (inc (count command-prefix)) (count text)))
                                             (reset! flag true))
                                           (command/set-input-message text)))
   :on-submit-editing (fn []
                        (when-not sending-disabled?
                          (dispatch [:send-command!])))
   :on-focus          #(dispatch [:set :focused true])
   :on-blur           #(dispatch [:set :focused true])
   :blur-on-submit    false})

(defn get-options [{:keys [type placeholder]} command-type]
  (let [options (case (keyword type)
                  :phone {:keyboard-type "phone-pad"}
                  :password {:secure-text-entry true}
                  :number {:keyboard-type "numeric"}
                  nil)]
    (if (= :response command-type)
      (if placeholder
        (assoc options :placeholder placeholder)
        options)
      (assoc options :placeholder ""))))

(defn get-selection
  [{:keys [focused? flag input-command command? just-set?]}]
  (cond (and command?
             (= input-command command-prefix)
             focused?
             (not just-set?))
        {:start 2
         :end   2}

        (and focused? flag)
        {:start 3
         :end   3}

        :else nil))

(defview message-input [set-layout-size]
  [input-message [:get-chat-input-text]
   input-command [:get-chat-command-content]
   command [:get-chat-command]
   icon-width [:command-icon-width]
   disable? [:get :disable-input]
   active? [:chat :is-active]
   command? [:command?]
   parameter [:get-command-parameter]
   type [:command-type]
   just-set? [:get :just-set-command?]
   sending-disabled? [:chat-ui-props :sending-disabled?]
   state {:input          (atom nil)
          :focused?       (atom nil)
          :3-symbols-flag (r/atom false)}]
  {:component-did-update (fn []
                           (when just-set?
                             (dispatch [:set :just-set-command? false]))
                           (when @(:3-symbols-flag state)
                             (reset! (:3-symbols-flag state) false))
                           (when (and command?
                                      (not (:fullscreen command)))
                             (.focus @(:input state))))}

  (do
    @(:3-symbols-flag state)
    [text-input
     (merge
       (if command?
         (command-input-options {:icon-width        icon-width
                                 :disable?          disable?
                                 :sendind-disabled? sending-disabled?
                                 :flag              (:3-symbols-flag state)})
         (plain-input-options {:set-layout-size-fn set-layout-size
                               :disable?           (or disable? (not active?))}))
       {:placeholder-text-color :#c0c5c9
        :onFocus                #(reset! (:focused? state) true)
        :onBlur                 #(reset! (:focused? state) false)
        :auto-focus             (when command? (not (:fullscreen command)))
        :ref                    #(reset! (:input state) %)
        :accessibility-label    id/chat-message-input
        ;; for some reason app crashes when this property is not nil and
        ;; input is not focused
        :selection              (get-selection {:focused?      @(:focused? state)
                                                :flag          @(:3-symbols-flag state)
                                                :input-command input-command
                                                :command?      command?
                                                :just-set?     just-set?})
        :default-value          (if command?
                                  (or input-command "")
                                  (or input-message ""))}
       (when command?
         (get-options parameter type)))]))

(defn plain-message-get-initial-state [_]
  {:height 0})

(defn plain-message-input-view []
  (let [command?             (subscribe [:command?])
        command              (subscribe [:get-chat-command])
        input-command        (subscribe [:get-chat-command-content])
        input-message        (subscribe [:get-chat-input-text])
        valid-plain-message? (subscribe [:valid-plain-message?])
        component            (r/current-component)
        set-layout-size      #(r/set-state component {:height %})
        sending-disabled?    (subscribe [:chat-ui-props :sending-disabled?])]
    (r/create-class
      {:get-initial-state
       plain-message-get-initial-state
       :component-will-update
       (fn [_]
         (when (or (and @command? (str/blank? @input-command))
                   (and (not @command?) (not @input-message)))
           (set-layout-size 0)))
       :reagent-render
       (fn []
         (let [{:keys [height]} (r/state component)]
           [view st/input-container
            [view (st/input-view height)
             [plain-message/commands-button height #(set-layout-size 0)]
             [view (st/message-input-container height)
              [message-input set-layout-size]]
             [plain-message/smile-button height]
             (when (or (and @command? (not (str/blank? @input-command)))
                       @valid-plain-message?)
               (let [on-press (if @command?
                                #(dispatch [:send-command!])
                                plain-message/send)]
                 [send-button {:on-press            (fn [e]
                                                      (when-not @sending-disabled?
                                                        (dispatch [:set-chat-ui-props :show-emoji? false])
                                                        (on-press e)))
                               :accessibility-label id/chat-send-button}]))
             (when (and @command? (= :command (:type @command)))
               [command/command-icon @command])]]))})))
