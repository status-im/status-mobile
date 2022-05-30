(ns status-im.navigation.state)

(defonce root-comp-id (atom nil))
(defonce root-id (atom nil))
(defonce pushed-screen-id (atom nil))
(defonce curr-modal (atom nil))
(defonce modals (atom []))
(defonce dissmissing (atom false))