(ns env.ios.main
  (:require [status-im.ios.core :as core]
            [re-frame.interop :as interop]
            [reagent.impl.batching :as batching]))

(set! interop/next-tick js/setTimeout)
(set! batching/fake-raf #(js/setTimeout % 0))

(core/init)