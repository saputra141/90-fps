package com.master.emulator.engine

import java.util.*
import kotlin.random.Random

class Chip8Core {
    val memory = IntArray(4096)
    val v = IntArray(16) // Registers
    var i = 0 // Index register
    var pc = 0x200 // Program counter
    val stack = Stack<Int>()
    val display = IntArray(64 * 32)
    var delayTimer = 0
    var soundTimer = 0
    val keys = BooleanArray(16)

    init {
        val fontSet = intArrayOf(
            0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
            0x20, 0x60, 0x20, 0x20, 0x70, // 1
            0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
            0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
            0x90, 0x90, 0xF0, 0x10, 0x10, // 4
            0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
            0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
            0xF0, 0x10, 0x20, 0x40, 0x40, // 7
            0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
            0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
            0xF0, 0x90, 0xF0, 0x90, 0x90, // A
            0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
            0xF0, 0x80, 0x80, 0x80, 0xF0, // C
            0xE0, 0x90, 0x90, 0x90, 0xE0, // D
            0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
            0xF0, 0x80, 0xF0, 0x80, 0x80  // F
        )
        for (idx in fontSet.indices) {
            memory[idx] = fontSet[idx]
        }
    }

    fun loadRom(rom: ByteArray) {
        for (idx in rom.indices) {
            memory[0x200 + idx] = rom[idx].toInt() and 0xFF
        }
    }

    fun cycle() {
        val opcode = (memory[pc] shl 8) or memory[pc + 1]
        val x = (opcode and 0x0F00) shr 8
        val y = (opcode and 0x00F0) shr 4
        val nn = opcode and 0x00FF
        val nnn = opcode and 0x0FFF

        pc += 2

        when (opcode and 0xF000) {
            0x0000 -> when (nn) {
                0xE0 -> display.fill(0)
                0xEE -> pc = stack.pop()
            }
            0x1000 -> pc = nnn
            0x2000 -> {
                stack.push(pc)
                pc = nnn
            }
            0x3000 -> if (v[x] == nn) pc += 2
            0x4000 -> if (v[x] != nn) pc += 2
            0x5000 -> if (v[x] == v[y]) pc += 2
            0x6000 -> v[x] = nn
            0x7000 -> v[x] = (v[x] + nn) and 0xFF
            0x8000 -> when (opcode and 0x000F) {
                0x0 -> v[x] = v[y]
                0x1 -> v[x] = v[x] or v[y]
                0x2 -> v[x] = v[x] and v[y]
                0x3 -> v[x] = v[x] xor v[y]
                0x4 -> {
                    val sum = v[x] + v[y]
                    v[0xF] = if (sum > 0xFF) 1 else 0
                    v[x] = sum and 0xFF
                }
                0x5 -> {
                    v[0xF] = if (v[x] > v[y]) 1 else 0
                    v[x] = (v[x] - v[y]) and 0xFF
                }
                0x6 -> {
                    v[0xF] = v[x] and 0x1
                    v[x] = v[x] shr 1
                }
                0x7 -> {
                    v[0xF] = if (v[y] > v[x]) 1 else 0
                    v[x] = (v[y] - v[x]) and 0xFF
                }
                0xE -> {
                    v[0xF] = (v[x] and 0x80) shr 7
                    v[x] = (v[x] shl 1) and 0xFF
                }
            }
            0x9000 -> if (v[x] != v[y]) pc += 2
            0xA000 -> i = nnn
            0xB000 -> pc = nnn + v[0]
            0xC000 -> v[x] = Random.nextInt(256) and nn
            0xD000 -> {
                val height = opcode and 0x000F
                v[0xF] = 0
                for (row in 0 until height) {
                    var spriteByte = memory[i + row]
                    for (col in 0 until 8) {
                        if ((spriteByte and 0x80) != 0) {
                            val pixelIdx = ((v[x] + col) % 64) + (((v[y] + row) % 32) * 64)
                            if (display[pixelIdx] == 1) v[0xF] = 1
                            display[pixelIdx] = display[pixelIdx] xor 1
                        }
                        spriteByte = spriteByte shl 1
                    }
                }
            }
            0xE000 -> when (nn) {
                0x9E -> if (keys[v[x] and 0xF]) pc += 2
                0xA1 -> if (!keys[v[x] and 0xF]) pc += 2
            }
            0xF000 -> when (nn) {
                0x07 -> v[x] = delayTimer
                0x15 -> delayTimer = v[x]
                0x18 -> soundTimer = v[x]
                0x1E -> i = (i + v[x]) and 0xFFFF
                0x29 -> i = (v[x] and 0xF) * 5
                0x33 -> {
                    memory[i] = v[x] / 100
                    memory[i + 1] = (v[x] / 10) % 10
                    memory[i + 2] = v[x] % 10
                }
                0x55 -> for (idx in 0..x) memory[i + idx] = v[idx]
                0x65 -> for (idx in 0..x) v[idx] = memory[i + idx]
            }
        }
    }

    fun updateTimers() {
        if (delayTimer > 0) delayTimer--
        if (soundTimer > 0) soundTimer--
    }
}
