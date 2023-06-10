(ns app.interface.keyboard
  (:require [keybind.core :as key]
            [re-frame.core :as rf]))

; See documentation at https://github.com/piranha/keybind

(key/bind! "enter" :pass-turn-keybind #(rf/dispatch [:pass-turn]))
