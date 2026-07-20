# How to restore a safe point

If a change breaks the app, you can always go back.

## Safety checkpoint (before these fixes)

```bash
git checkout safe-checkpoint-v1.2.0
```

That tag points at **v1.2.0** (`16538ae`) — the last known good state before the security/stability fixes.

## Return to latest work

```bash
git checkout main
```

## See all safety tags / fix commits

```bash
git tag -l "safe*"
git log --oneline
```

## Note

- Do **not** force-push or delete tags unless you intend to.
- Signing passwords live only in `local.properties` (never committed).
- See `FIXES.md` for a chronological list of every fix after the checkpoint.
