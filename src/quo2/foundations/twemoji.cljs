(ns quo2.foundations.twemoji
  (:require [reagent.core :as reagent]))

(def ^:private twemoji-js (js/require "../src/js/components/twemoji.js"))

;; For rendering single Twemoji
;; Usage in: Account avatar, Channel avatar, Emoji hash and Emoji picker
(def twemoji (reagent/adapt-react-class (.-Twemoji ^js twemoji-js)))

;; For parsing the string and replacing emoji with Twemoji equivalent
;; Usage in: Chat
(def text (reagent/adapt-react-class (.-TwemojiText ^js twemoji-js)))
