/*
 * java-tron is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-tron is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.tron.core.capsule;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.ECKey.ECDSASignature;
import org.tron.common.utils.ByteArray;
import org.tron.core.Sha256Hash;
import org.tron.core.peer.Validator;
import org.tron.protos.Protocal.Block;
import org.tron.protos.Protocal.BlockHeader;


public class BlockCapsule {

  protected static final Logger logger = LoggerFactory.getLogger("BlockCapsule");

  private byte[] data;

  private Block block;

  private Sha256Hash hash;


  private boolean unpacked;

  private synchronized void unPack() {
    if (unpacked) {
      return;
    }

    try {
      this.block = Block.parseFrom(data);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage());
    }

    unpacked = true;
  }

  private void sign(String privateKey) {
    // TODO private_key == null
    ECKey ecKey = ECKey.fromPrivate(Hex.decode(privateKey));
    String pubKey = ByteArray.toHexString(ecKey.getPubKey());

    ECDSASignature signature = ecKey.sign(hash.getBytes());
    ByteString sig = ByteString.copyFrom(signature.toByteArray());

    BlockHeader blockHeader = this.block.getBlockHeader().toBuilder().setWitnessSignature(sig)
        .build();

    this.block.toBuilder().setBlockHeader(blockHeader);

  }

  // TODO
  private boolean validateSigner() {
    return true;
  }

  private void hash() {
    this.data = this.block.toByteArray();
    BlockHeader blockHeader = block.getBlockHeader().toBuilder()
        .setHash(Sha256Hash.of(this.data).getByteString()).build();
    this.block.toBuilder().setBlockHeader(blockHeader).build();
  }

  private void calcMerkleRoot() {

  }

  private void pack() {
    if (data == null) {
      this.data = this.block.toByteArray();
      this.hash = Sha256Hash.of(this.data);
    }
  }

  public boolean validate() {
    unPack();
    return Validator.validate(this.block);
  }

  public BlockCapsule(Block block) {
    this.block = block;
    unpacked = true;
  }

  public BlockCapsule(byte[] data) {
    this.data = data;
    this.hash = Sha256Hash.of(this.data);
    unpacked = false;
  }

  public byte[] getData() {
    pack();
    return data;
  }

  public Sha256Hash getParentHash() {
    unPack();
    return Sha256Hash.wrap(this.block.getBlockHeader().getParentHash());
  }

  public Sha256Hash getHash() {
    pack();
    return hash;
  }


  public long getNum() {
    unPack();
    return this.block.getBlockHeader().getNumber();
  }

}
