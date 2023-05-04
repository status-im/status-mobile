(ns status-im2.contexts.chat.bottom-sheet-composer.selectable-input.view
  (:require [utils.i18n :as i18n]))

(declare first-level-menu-items second-level-menu-items)

(defn calculate-input-text
  [{:keys [full-text selection-start selection-end]} content]
  (let [head (subs full-text 0 selection-start)
        tail (subs full-text selection-end)]
    (str head content tail)))

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
    ;(update-input-text params new-text)
    (reset-to-first-level-menu first-level menu-items)))


(def second-level-menus
  {:bold          #(append-markdown-char % "**")

   :italic        #(append-markdown-char % "*")

   :strikethrough #(append-markdown-char % "~~")})

(def second-level-menu-items (map i18n/label (keys second-level-menus)))
