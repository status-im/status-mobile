(ns status-im2.contexts.chat.bottom-sheet-composer.selection
  (:require
    [clojure.string :as string]
    [react-native.clipboard :as clipboard]
    [react-native.core :as rn]
    [oops.core :as oops]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.transforms :as transforms]))

(declare first-level-menu-items second-level-menu-items)

(defn update-input-text
  [{:keys [text-input]} text]
  (rf/dispatch [:chat.ui/set-chat-input-text text])
  (.setNativeProps ^js text-input (clj->js {:text text})))

(defn calculate-input-text
  [{:keys [full-text selection-start selection-end]} content]
  (let [head (subs full-text 0 selection-start)
        tail (subs full-text selection-end)]
    (str head content tail)))

(defn update-selection
  [text-input-handle selection-start selection-end]
  ;to avoid something like this
  ;https://lightrun.com/answers/facebook-react-native-textinput-controlled-selection-broken-on-both-ios-and-android
  ;use native invoke instead! do not use setNativeProps! e.g. (.setNativeProps ^js text-input (clj->js
  ;{:selection {:start selection-start :end selection-end}}))
  (let [manager (rn/selectable-text-input-manager)]
    (oops/ocall manager :setSelection text-input-handle selection-start selection-end)))

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

(def first-level-menus
  {:cut               (fn [{:keys [content] :as params}]
                        (let [new-text (calculate-input-text params "")]
                          (clipboard/set-string content)
                          (update-input-text params new-text)))

   :copy-to-clipboard (fn [{:keys [content]}]
                        (clipboard/set-string content))

   :paste             (fn [params]
                        (let [callback (fn [paste-content]
                                         (let [content  (string/trim paste-content)
                                               new-text (calculate-input-text params content)]
                                           (update-input-text params new-text)))]
                          (clipboard/get-string callback)))

   :biu               (fn [{:keys [first-level text-input-handle menu-items selection-start
                                   selection-end]}]
                        (reset! first-level false)
                        (reset! menu-items second-level-menu-items)
                        (update-selection text-input-handle selection-start selection-end))})

(def first-level-menu-items (map i18n/label (keys first-level-menus)))
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

(defn on-selection
  [event
   {:keys [input-ref selection-event]}
   {:keys [first-level menu-items]}]
  (let [native-event           (.-nativeEvent event)
        native-event           (transforms/js->clj native-event)
        {:keys [eventType content selectionStart
                selectionEnd]} native-event
        full-text              (:input-text (rf/sub [:chats/current-chat-input]))]
    (on-menu-item-touched {:first-level       first-level
                           :event-type        eventType
                           :content           content
                           :selection-start   selectionStart
                           :selection-end     selectionEnd
                           :text-input        @input-ref
                           :text-input-handle (rn/find-node-handle @input-ref)
                           :full-text         full-text
                           :menu-items        menu-items
                           :selection-event   selection-event})))
