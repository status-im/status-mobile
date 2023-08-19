(ns quo2.foundations.twemoji
  (:require [reagent.core :as reagent]))

(def ^:private twemoji-js (js/require "../src/js/components/twemoji.js"))

(def twemoji (reagent/adapt-react-class (.-Twemoji ^js twemoji-js)))
(def text (reagent/adapt-react-class (.-TwemojiText ^js twemoji-js)))
